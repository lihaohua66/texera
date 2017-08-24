package edu.uci.ics.textdb.perftest.sample;

import edu.uci.ics.textdb.api.exception.DataFlowException;
import edu.uci.ics.textdb.api.field.StringField;
import edu.uci.ics.textdb.api.field.TextField;
import edu.uci.ics.textdb.api.span.Span;
import edu.uci.ics.textdb.api.tuple.Tuple;
import edu.uci.ics.textdb.exp.dictionarymatcher.Dictionary;
import edu.uci.ics.textdb.exp.dictionarymatcher.DictionaryMatcher;
import edu.uci.ics.textdb.exp.dictionarymatcher.DictionaryPredicate;
import edu.uci.ics.textdb.exp.keywordmatcher.KeywordMatchingType;
import edu.uci.ics.textdb.exp.sink.excel.ExcelSink;
import edu.uci.ics.textdb.exp.sink.excel.ExcelSinkPredicate;
import edu.uci.ics.textdb.exp.sink.tuple.TupleSink;
import edu.uci.ics.textdb.exp.source.file.FileSourceOperator;
import edu.uci.ics.textdb.exp.source.file.FileSourcePredicate;
import edu.uci.ics.textdb.exp.udf.UdfOperator;
import edu.uci.ics.textdb.exp.udf.UdfPredicate;
import edu.uci.ics.textdb.perftest.promed.PromedSchema;
import edu.uci.ics.textdb.perftest.utils.PerfTestUtils;
import edu.uci.ics.textdb.storage.DataWriter;
import edu.uci.ics.textdb.storage.RelationManager;
import edu.uci.ics.textdb.storage.constants.LuceneAnalyzerConstants;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import au.com.bytecode.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class SampleExtraction {
    
    public static final String PROMED_SAMPLE_TABLE = "promed";
        
    public static String promedFilesDirectory = PerfTestUtils.getResourcePath("/sample-data-files/promed");
    public static String promedIndexDirectory = PerfTestUtils.getResourcePath("/index/standard/promed");
    public static String sampleDataFilesDirectory = PerfTestUtils.getResourcePath("sample-data-files");        
    
    
    public static void main(String[] args) throws Exception {
        // write the index of data files
        // index only needs to be written once, after the first run, this function can be commented out
   //     writeSampleIndex();

        // perform the extraction task
      //  extractPersonLocation();
        udfDo();
    }

    public static Tuple parsePromedHTML(String fileName, String content) {
        try {
            Document parsedDocument = Jsoup.parse(content);
            String mainText = parsedDocument.getElementById("preview").text();
            Tuple tuple = new Tuple(PromedSchema.PROMED_SCHEMA, new StringField(fileName), new TextField(mainText));
            return tuple;
        } catch (Exception e) {
            return null;
        }
    }

    public static void writeSampleIndex() throws Exception {
        // parse the original file
        File sourceFileFolder = new File(promedFilesDirectory);
        ArrayList<Tuple> fileTuples = new ArrayList<>();
        for (File htmlFile : sourceFileFolder.listFiles()) {
            StringBuilder sb = new StringBuilder();
            Scanner scanner = new Scanner(htmlFile);
            while (scanner.hasNext()) {
                sb.append(scanner.nextLine());
            }
            scanner.close();
            Tuple tuple = parsePromedHTML(htmlFile.getName(), sb.toString());
            if (tuple != null) {
                fileTuples.add(tuple);
            }
        }
        
        // write tuples into the table
        RelationManager relationManager = RelationManager.getRelationManager();
        
        relationManager.deleteTable(PROMED_SAMPLE_TABLE);
        relationManager.createTable(PROMED_SAMPLE_TABLE, promedIndexDirectory, 
                PromedSchema.PROMED_SCHEMA, LuceneAnalyzerConstants.standardAnalyzerString());
        
        DataWriter dataWriter = relationManager.getTableDataWriter(PROMED_SAMPLE_TABLE);
        dataWriter.open();
        for (Tuple tuple : fileTuples) {
            dataWriter.insertTuple(tuple);
        }
        dataWriter.close();
    }

    /*
     * This is the DAG of this extraction plan.
     * 
     * 
     *              KeywordSource (zika)
     *                       ↓
     *              Projection (content)
     *                  ↓          ↓
     *       regex (a...man)      NLP (location)
     *                  ↓          ↓     
     *             Join (distance < 100)
     *                       ↓
     *              Projection (spanList)
     *                       ↓
     *                    FileSink
     *                    
     */
    public static void udfDo() throws Exception {
        String tempFile1Path = "/tmp/adidas.txt";
//        String legalExtractionResultFile = "/tmp/legalExtractionResult.csv";
        
        FileSourcePredicate predicate = new FileSourcePredicate(
                tempFile1Path.toString(),"text");
        FileSourceOperator fileSource = new FileSourceOperator(predicate);
        
        UdfOperator udfOp = new UdfOperator(new UdfPredicate("text","result","    output = len(input)"));
        
        udfOp.setInputOperator(fileSource);
        
        TupleSink sink = new TupleSink();
        sink.setInputOperator(udfOp);
        
        sink.open();
        List<Tuple> result = sink.collectAllTuples();
        sink.close();
        
        for (Tuple tup:result) {
            System.out.print(tup.getField(1).getValue());
        }
    }
    public static void extractPersonLocation() throws Exception {
        // this sample extraction won't work because the CharacterDistanceJoin hasn't been updated yet
        // TODO: after Join is fixed, add this sample extraction back.
        String tempFile1Path = "/media/sjwn/B638-E4F5/Doc2txt/testResult";
        String legalExtractionResultFile = "/tmp/legalExtractionResult.csv";
        
        FileSourcePredicate predicate = new FileSourcePredicate(
                tempFile1Path.toString(),"text");
        FileSourceOperator fileSource = new FileSourceOperator(predicate);
        List<String> patterns = new ArrayList<>();
        List<String> items = new ArrayList<>();
        patterns.add(".{4,70}(案|书)\\n");
        items.add("S1案件名称");
        
        patterns.add(".*法\\p{Zs}{0,1}院\\n");
        items.add("S2法院");
        
        patterns.add(".{2,4}((判\\p{Zs}{0,1}决)|(裁\\p{Zs}{0,1}定)|(调\\p{Zs}{0,1}解))\\p{Zs}{0,1}书.{0,1}\\s*\\n");
        items.add("S3案件性质");
        
//        patterns.add("((\\(|\\（)\\d{4}(\\）|\\)))(京|津|冀|晋|内蒙古|辽|吉|黑|沪|申|苏|浙|皖|闽|赣|鲁|豫|鄂|湘|粤|桂|琼|川|蜀|贵|黔|云|滇|渝|藏|陕|秦|甘|陇|青|宁|新|港|澳|台).(|中|高)(知|刑|民)(初|终)字第\\d{1,5}号");
 //       patterns.add("((\\(|\\（)\\d{4}(\\）|\\)))(京|津|冀|晋|内蒙古|辽|吉|黑|沪|申|苏|浙|皖|闽|赣|鲁|豫|鄂|湘|粤|桂|琼|川|蜀|贵|黔|云|滇|渝|藏|陕|秦|甘|陇|青|宁|新|港|澳|台){0,1}.{1,5}(中|高){0,1}(知|刑|民)(初|终)字第\\d{1,5}号");
        patterns.add("(\\〔|\\(|（)\\d{4}(|\\〕|\\))(京|津|冀|晋|内蒙古|辽|吉|黑|沪|申|苏|浙|皖|闽|赣|鲁|豫|鄂|湘|粤|桂|琼|川|蜀|贵|黔|云|滇|渝|藏|陕|秦|甘|陇|青|宁|新|港|澳|台){0,1}.{0,5}(中|高){0,1}(执|经|知|刑|民).{0,3}(初|终|禁){0,1}字{0,1}第{0,1}\\d{1,5}-{0,1}\\d{0,2}号\\s{0,3}\\n");
        items.add("S4年S5省S6市S7法院层级S8法庭类别S9案件审批级别S10案号");
        
        patterns.add("((\\p{Zs}{1,10})|(\\s{1,10}))((原告)|(申请人)|(上诉人)).{2,30}(，|,|(\\n))");
        items.add("S11原告");
  //      (\s{1,5}|\p{Zs}{1,5})原告(.{2,20}(\n|，|,))
        patterns.add("((\\p{Zs}{1,10})|(\\s{1,10}))((被告)|(被申请人)|(被上诉人)).{2,30}\\n");
        items.add("S14被告");
        
        patterns.add("\\p{Zs}{2,10}委托代理人.{2,30}\\n");
        items.add("委托代理人");
//        patterns.add("被告.{20}\\n");
        patterns.add("\\d{4}年\\d{1,2}月\\d{1,2}日.{0,10}起诉");
        items.add("S16起诉日");
        
        patterns.add("\\d{4}年\\d{1,2}月\\d{1,2}日.{0,18}开庭");
        items.add("S17开庭日");
        
        patterns.add("本案现{0,1}已{0,1}审理.{2}");
        items.add("S18案件审理情况");
        
        patterns.add("我.{1,5}知识产权局.{1,3}\\“.{2,15}\\”");
        items.add("S19涉案专利名称");
        
        patterns.add("申请号为\\d{12}.\\d");
        items.add("S21涉案专利申请号");
        
        patterns.add("\\d{4}年\\d{1,2}月\\d{1,2}日获得{0,1}授权");
        items.add("涉案专利授权日");
        
        patterns.add("赔偿.{2,10}万{0,1}\\d{0,2}千{0,1}元.{15}元{0,1}");
        items.add("S24原告请求的赔偿额");
        
        patterns.add("依照《中华人民共和国.*法》.*规定");
        items.add("S25_S29本案适用的法条");
                                        
        patterns.add("赔偿.{2,10}万{0,1}\\d{0,2}千{0,1}元");
        items.add("S30本案判定的赔偿额");
                                                
        patterns.add("本案案件受理费.* 元");
        items.add("S31本案的审判费用");
                                                        
        patterns.add("受理费.*由.*负担");
        items.add("S32承担本案审判费用的当事人");
        
        Dictionary dic = new Dictionary(patterns);
        List<String> attributeNames = new ArrayList<>();
        attributeNames.add("text");
        
        DictionaryMatcher dictionaryMatcher = new DictionaryMatcher(new DictionaryPredicate(dic,
                attributeNames, LuceneAnalyzerConstants.chineseAnalyzerString(),
                KeywordMatchingType.SUBSTRING_SCANBASED, "result"));
        dictionaryMatcher.setInputOperator(fileSource);
        
        UdfOperator udfOp = new UdfOperator(new UdfPredicate("input", "output","select(1)"));
        
        
        TupleSink sink = new TupleSink();
        sink.setInputOperator(dictionaryMatcher);
        
        sink.open();
        List<Tuple> result = sink.collectAllTuples();
        sink.close();
        System.out.println(result.size());
        System.out.println(result.get(0).getField("text").getValue().toString());
        int cnt = 0;
        List<String[]> csvData = new ArrayList<>();
        for(Tuple tup : result) {
            System.out.println("In  "+cnt+ " ************************");
            int i = 1;
            String prtResult = "";
            String ptnKey = "";
            cnt +=1;
            String[] idTextPair = new String[40];
            int csvDataIndex = 0;
            for(Span s : (List<Span>)tup.getField("result").getValue()){
                if (s.getKey().toString().equals(ptnKey)) {
                    if (prtResult.contains(s.getValue()) || s.getValue().contains(prtResult) ) {
                        ;
                    }
                    else {
                        prtResult += "_" +s.getValue();
                    }
                } else {
                    //System.out.println(i+": " );
                    if (prtResult.length() != 0){
                        System.out.println(prtResult );
                        idTextPair[csvDataIndex] = prtResult;
                    }
                    prtResult = s.getValue();
                    ptnKey = s.getKey().toString();
                    int index = patterns.indexOf(ptnKey);
                    csvDataIndex = index;
                    prtResult = items.get(index) + " : " + prtResult;
                    
//                    prtResult = s.getValue();
                    i++;
                }
//                ;// "+s.getKey().toString()+"======> "+s.getValue());
               
            }
            csvData.add(idTextPair);
        }
        System.out.println(cnt);
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(legalExtractionResultFile));
            writer.writeAll(csvData);
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw new DataFlowException(e.getMessage(), e);
        }
        } 

}
