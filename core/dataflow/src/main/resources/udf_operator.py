#!/usr/bin/python3
# -*- coding: utf-8 -*-

import mmap
import os
import signal
from time import sleep
import json
import sys

JSON_Datalist = {
        "schema":{"attributes": [{"attributeName":"content", 
                                  "attributeType":"text"}, 
                                 {"attributeName":"payload",
                                  "attributeType":"list"}]},
        "fields": [{"value":"test1"}, 
                   {"value":[{"attributeName":"content",
                              "start":0,
                              "end":4,
                              "key":"test",
                              "value":"test",
                              "tokenOffset":0}]}]}
inputFullPathFileName = sys.argv[1]
outputFullPathFileName = sys.argv[2]
my_length = 0
n2one = True
sig_wait = True # initiate state for N:1
sig_output = 'w'
sig_input = ''

class TupleOperator:
    def __init__(self):
        #hands shaking step
        self.tuple_dict = {}
        self.output_tuple_dict = {}
        self.javapid = -1
        self.f_input = open(inputFullPathFileName, 'r+b')
        self.map_input = mmap.mmap(self.f_input.fileno(),0)
        self.pythonpid = os.getpid()
        self.map_input.seek(0)
        self.javapid = int(self.map_input.readline())
        
        self.f_output = open(outputFullPathFileName,'r+b')
        self.map_output = mmap.mmap(self.f_output.fileno(), 0)
        self.map_output.seek(0)
        self.map_output.write(bytes(str(self.pythonpid)+"\n", 'utf-8'))
        print("Hands shake begin")
        os.kill(self.javapid, signal.SIGUSR2)
        print("success: Hands shake end")

        
    def string_2_dict(self, string):
        self.tuple_dict = json.loads(json.dumps(str(string)))
        
    def add_field(self, attrName, attrType, value):
        #add schema
        attr = {"attributeName":attrName, "attributeType":attrType}
        (self.output_tuple_dict['schema']['attributes']).append(attr)
        #add field
        field = {"value": value}
        (self.output_tuple_dict['fields']).append(field)
        
    def get_valueByAttribute(self, attrname):
        print("get value By Attr")
        print(attrname)
        print(self.tuple_dict['schema'])
        for att,v in zip(self.tuple_dict['schema']['attributes'], self.tuple_dict['fields']):
            for attname,vv in att.items():
                if (vv == attrname):
                    return v['value']
        return None
    
    def read_input(self):
        global sig_input
        global sig_output
        self.map_input.seek(10)
        sig_input = self.map_input.readline().rstrip()
#        bool_output = False
        print(sig_input)
        if(sig_input == b'0'):
            print("Should output 000000000000000000000000000000000")
        else:
            textlen_input = sig_input
            self.map_input.seek(20)
            content_input = self.map_input.read(int(textlen_input))
            self.tuple_dict = json.loads(content_input.decode('utf-8'))
            sig_input = 't'
        
    def write_output(self):
        global sig_input
        global sig_output
        if (sig_output == 't'):
#            self.output_tuple_dict = self.tuple_dict
            content_output = json.dumps(self.output_tuple_dict)
            sig_output = str(len(content_output))
            self.map_output.seek(10)
            textlen_output = len(content_output)
            self.map_output.write(bytes(str(textlen_output),'utf-8'))
            self.map_output.seek(20)
            self.map_output.write(content_output.encode('utf-8'))
            sig_output = 't'

        if (sig_output == '0'):
            self.map_output.seek(10)
            self.map_output.write('0'.encode('utf-8'))
            
        if (sig_output == 'w'):
            self.map_output.seek(10)
            self.map_output.write('w'.encode('utf-8'))
        #if (sig_ouput == '#'):
            #do nothing, just let sig_output be undifined.
        self.tuple_dict = {}
        os.kill(self.javapid, signal.SIGUSR2)

    def do_sig(self):
        self.read_input()
        ##############################################################
        ###user defined function here
        
        global n2one
        global sig_output
        global my_length

        n2one = False
        if (n2one == True):
            #This demo will compute total length of field content for all tuple 
            if (sig_input == b'0' and sig_output == 't'):
                #send end signal null
                sig_output = '0'
            if (sig_input == b'0' and sig_output == 'w'):
                #send signal to write tuple
                sig_output = 't'
                attrType =  "text"
                value = my_length
                new_attrName = "length"
                self.add_field(new_attrName, attrType, value)
            if (sig_input == 't' and sig_output == 'w'):
                #send signal to wait
                sig_output = 'w'
                #do caculation
                attrName = "content"
                attrType = "text"
                
                for att,v in zip(self.tuple_dict['schema']['attributes'], self.tuple_dict['fields']):
                    for attname,vv in att.items():
                        if (vv == attrName):
                            input_fieldValue = v['value']
                value = len(input_fieldValue)
                self.output_tuple_dict = self.tuple_dict
                
                my_length = my_length + value
                    
        if(n2one == False):
            #we need to construct a output dict
            #this demo will compute length of field "content"
            input_fieldValue = ''
            if (sig_output == 'w'):
                sig_output = 't'
            if (sig_input == 't'):
                #send signal to write tuple
                self.output_tuple_dict = self.tuple_dict
                attrName = "content"
                attrType = "text"
                for att,v in zip(self.tuple_dict['schema']['attributes'], self.tuple_dict['fields']):
                    for attname,vv in att.items():
                        if (vv == attrName):
                            input_fieldValue = v['value']
                value = len(input_fieldValue)
                new_attrName = "local_length"
                self.add_field(new_attrName, attrType, value)
                sig_output = 't'
            if (sig_input == b'0'):
                #send signal of null
                sig_output = '0'
    ####################################################
    ## output Part
        self.write_output()
        
    def get_nextTupleText(self):
        self.map_input.seek(10)
        text_len = self.map_input.readline().rstrip()
        if (text_len == 0):
            return None
        self.map_input.seek(20)
        content_input = self.map_input.read(int(text_len))
        self.tuple_dict = json.loads(content_input.decode('utf-8'))
        return text_len
        #this is User defined function
        
    def populate_output(self):
        #this is a example User defined function to state the lenght of a field.
        # if length >10, ouput.
#        self.tuple_dict = json.loads(content_input.decode('utf-8'))
        input_fieldValue = self.getvalueByAttribute('content')
        attrName = "length"
        attrType = "text"
        value = len(input_fieldValue)
        
        #we need to construct a output dict
        self.output_tuple_dict = self.tuple_dict
        self.add_field(attrName, attrType, value)
        return value>0
    
    def get_fieldvalue(self, field):
        value = self.tuple_dict['schema']['attributes']
        return value

    def onsignal_usr2(self, a,b):
        self.do_sig()
        
    def close(self):
        self.map.close()
        self.map_output.close()


def main():
    tuple_operator = TupleOperator()
#    tuple_operator.do_sig()     #        signal.signal(signal.SIGUSR2, do_sig(self))
    signal.signal(signal.SIGUSR2, tuple_operator.onsignal_usr2)
    print("Python loop start")
    while 1:
        sleep(1)
								
if __name__ == "__main__":
    # execute only if run as a script
    main()