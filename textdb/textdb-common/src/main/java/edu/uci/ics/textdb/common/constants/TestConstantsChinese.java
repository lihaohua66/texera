package edu.uci.ics.textdb.common.constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import edu.uci.ics.textdb.api.common.Attribute;
import edu.uci.ics.textdb.api.common.FieldType;
import edu.uci.ics.textdb.api.common.IField;
import edu.uci.ics.textdb.api.common.ITuple;
import edu.uci.ics.textdb.api.common.Schema;
import edu.uci.ics.textdb.common.field.DataTuple;
import edu.uci.ics.textdb.common.field.DateField;
import edu.uci.ics.textdb.common.field.DoubleField;
import edu.uci.ics.textdb.common.field.IntegerField;
import edu.uci.ics.textdb.common.field.StringField;
import edu.uci.ics.textdb.common.field.TextField;

/**
 * @author sandeepreddy602
 * @author Qinhua Huang
 *         Chinese data constant, used by unit test for Chinese.
 */
public class TestConstantsChinese {
    // Sample Fields
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    public static final String AGE = "age";
    public static final String HEIGHT = "height";
    public static final String DATE_OF_BIRTH = "dateOfBirth";
    public static final String DESCRIPTION = "description";

    public static final Attribute FIRST_NAME_ATTR = new Attribute(FIRST_NAME, FieldType.STRING);
    public static final Attribute LAST_NAME_ATTR = new Attribute(LAST_NAME, FieldType.STRING);
    public static final Attribute AGE_ATTR = new Attribute(AGE, FieldType.INTEGER);
    public static final Attribute HEIGHT_ATTR = new Attribute(HEIGHT, FieldType.DOUBLE);
    public static final Attribute DATE_OF_BIRTH_ATTR = new Attribute(DATE_OF_BIRTH, FieldType.DATE);
    public static final Attribute DESCRIPTION_ATTR = new Attribute(DESCRIPTION, FieldType.TEXT);

    // Sample Schema
    public static final Attribute[] ATTRIBUTES_PEOPLE = { FIRST_NAME_ATTR, LAST_NAME_ATTR, AGE_ATTR, HEIGHT_ATTR,
            DATE_OF_BIRTH_ATTR, DESCRIPTION_ATTR };
    public static final Schema SCHEMA_PEOPLE = new Schema(ATTRIBUTES_PEOPLE);

    public static List<ITuple> getSamplePeopleTuples() {
        
        try {
            IField[] fields1 = { new StringField("无忌"), new StringField("长孙"), new IntegerField(46),
                    new DoubleField(5.50), new DateField(new SimpleDateFormat("MM-dd-yyyy").parse("01-14-1970")),
                    new TextField("北京大学电气工程学院") };
            IField[] fields2 = { new StringField("孔明"), new StringField("洛克贝尔"),
                    new IntegerField(42), new DoubleField(5.99),
                    new DateField(new SimpleDateFormat("MM-dd-yyyy").parse("01-13-1974")), new TextField("北京大学计算机学院") };
            IField[] fields3 = { new StringField("宋江"), new StringField("建筑"),
                    new IntegerField(42), new DoubleField(5.99),
                    new DateField(new SimpleDateFormat("MM-dd-yyyy").parse("01-13-1974")), 
                    new TextField("伟大的建筑是历史的坐标，具有传承的价值。") };
           
            ITuple tuple1 = new DataTuple(SCHEMA_PEOPLE, fields1);
            ITuple tuple2 = new DataTuple(SCHEMA_PEOPLE, fields2);
            ITuple tuple3 = new DataTuple(SCHEMA_PEOPLE, fields3);
            
            return Arrays.asList(tuple1, tuple2, tuple3);
        } catch (ParseException e) {
            // exception should not happen because we know the data is correct
            e.printStackTrace();
            return Arrays.asList();
        }

    }
}
