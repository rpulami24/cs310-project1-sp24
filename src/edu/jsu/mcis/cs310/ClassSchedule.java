package edu.jsu.mcis.cs310;

import com.github.cliftonlabs.json_simple.*;
import com.opencsv.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.*;

public class ClassSchedule {
    
    private final String CSV_FILENAME = "jsu_sp24_v1.csv";
    private final String JSON_FILENAME = "jsu_sp24_v1.json";
    
    private final String CRN_COL_HEADER = "crn";
    private final String SUBJECT_COL_HEADER = "subject";
    private final String NUM_COL_HEADER = "num";
    private final String DESCRIPTION_COL_HEADER = "description";
    private final String SECTION_COL_HEADER = "section";
    private final String TYPE_COL_HEADER = "type";
    private final String CREDITS_COL_HEADER = "credits";
    private final String START_COL_HEADER = "start";
    private final String END_COL_HEADER = "end";
    private final String DAYS_COL_HEADER = "days";
    private final String WHERE_COL_HEADER = "where";
    private final String SCHEDULE_COL_HEADER = "schedule";
    private final String INSTRUCTOR_COL_HEADER = "instructor";
    private final String SUBJECTID_COL_HEADER = "subjectid";
    
    public String convertCsvToJsonString(List<String[]> csv) {
        /*
            "crn" "subject" "num" "description" "section" "type" "credits" "start" "end" "days" "where" "schedule" "instructor"
            "21095" "Computer Science" "CS 304" "Technical Writing for Computer Science (WI)" "001" "LEC" "3" "09:15:00" "10:45:00" "TR" "Ayers Hall 355" "In-Person Instruction" "Cynthia Gunter Jensen"
        */
        
        JsonObject result = new JsonObject();
        
        /*
            "scheduletype": {
                "LEC": "In-Person Instruction",
                "ONL": "Online (asynchronous)"
            }
        */
        JsonObject scheduleTypeObj = new JsonObject();
        
        /*
            "course": {
                "CS 304": {
                    "subjectid": "CS",
                    "num": "304",
                    "description": "Technical Writing for Computer Science (WI)",
                    "credits": 3
                },
                "CS 308": {
                    "subjectid": "CS",
                    "num": "308",
                    "description": "Embedded and Control Systems Security",
                    "credits": 3
                },
                "CS 310": {
                    "subjectid": "CS",
                    "num": "310",
                    "description": "Software Engineering I",
                    "credits": 3
                }
            }
        */
        JsonObject courseObj = new JsonObject();
        
        /*
            "subject": {
                "CS": "Computer Science",
                "EH": "English",
                "HY": "History",
                "MS": "Mathematics"
            }
        */
        JsonObject subjectObj = new JsonObject();
        
        /*
            "section": [
               {
                   "crn": 21095,
                   "subjectid": "CS",
                   "num": "304",
                   "section": "001",
                   "type": "LEC",
                   "start": "09:15:00",
                   "end": "10:45:00",
                   "days": "TR",
                   "where": "Ayers Hall 355",
                   "instructor": [
                       "Cynthia Gunter Jensen"
                   ]
               },
           ]
        */
        ArrayList<JsonObject> sectionList = new ArrayList();     
        
        // will store each row for each iteration
        String[] csvRow;    
        
        Iterator<String[]> iterator = csv.iterator();      
        
        // grabbing the header row
        csvRow = iterator.next();      
        
        // to simplify grabbing data within the array (csvRow)
        LinkedHashMap<String, Integer> header = new LinkedHashMap<>(); 
        
        for(int i = 0; i < csvRow.length; i++){
            header.put(csvRow[i], i);   
        }
        
        while(iterator.hasNext()){
                   
                csvRow = iterator.next();    
                
                /*
                    #1 "scheduletype" 
                */
                String scheduletype = csvRow[header.get(TYPE_COL_HEADER)];
                if (!scheduleTypeObj.containsKey(scheduletype)) {     
                    scheduleTypeObj.put(scheduletype, csvRow[header.get(SCHEDULE_COL_HEADER)]);    
                }
                
                /*
                    #2 "course" 
                */
                JsonObject courseItem = new JsonObject(); 
                
                // "CS 304" -> ["CS", "304"]
                String[] courseCode = csvRow[header.get(NUM_COL_HEADER)].split(" "); 
                
                courseItem.put(SUBJECTID_COL_HEADER, courseCode[0]);  
                courseItem.put(NUM_COL_HEADER, courseCode[1]);   
                courseItem.put(DESCRIPTION_COL_HEADER, csvRow[header.get(DESCRIPTION_COL_HEADER)]); 
                courseItem.put(CREDITS_COL_HEADER, Integer.parseInt(csvRow[header.get(CREDITS_COL_HEADER)])); 
                
                courseObj.put(csvRow[header.get(NUM_COL_HEADER)], courseItem);
                
                /*
                    #3 "subject" 
                */
                
                if(!subjectObj.containsKey(courseCode[0])){      
                    subjectObj.put(courseCode[0], csvRow[header.get(SUBJECT_COL_HEADER)]);  
                }
                
                /*
                    #4 "section" 
                */
                JsonObject sectionItem = new JsonObject();  

                sectionItem.put(CRN_COL_HEADER,  Integer.parseInt(csvRow[header.get(CRN_COL_HEADER)]));  
                sectionItem.put(SUBJECTID_COL_HEADER, courseCode[0]);   
                sectionItem.put(NUM_COL_HEADER, courseCode[1]);    
                sectionItem.put(SECTION_COL_HEADER, csvRow[header.get(SECTION_COL_HEADER)]);    
                sectionItem.put(TYPE_COL_HEADER, csvRow[header.get(TYPE_COL_HEADER)]);     
                sectionItem.put(START_COL_HEADER, csvRow[header.get(START_COL_HEADER)]);     
                sectionItem.put(END_COL_HEADER, csvRow[header.get(END_COL_HEADER)]);  
                sectionItem.put(DAYS_COL_HEADER, csvRow[header.get(DAYS_COL_HEADER)]);     
                sectionItem.put(WHERE_COL_HEADER, csvRow[header.get(WHERE_COL_HEADER)]);  
                
                // EDGE CASE: Course Instructors can be MORE THAN ONE
                // "Teje H Sult, Dana Lynn Ingalsbe"
                // after processing: instructors = ["Teje H Sult", "Dana Lynn Ingalsbe"]
                String[] instructors = csvRow[header.get(INSTRUCTOR_COL_HEADER)].split(", ");
                sectionItem.put(INSTRUCTOR_COL_HEADER, instructors);      
                
                sectionList.add(sectionItem);   
        }
        
        result.put("subject", subjectObj);   
        result.put("section", sectionList); 
        result.put("course", courseObj);      
        result.put("scheduletype", scheduleTypeObj);   
        
        
        return Jsoner.serialize(result);
        
    }
    
    public String convertJsonToCsvString(JsonObject json) {
        /*
            "scheduletype": {
                "LEC": "In-Person Instruction",
                "ONL": "Online (asynchronous)"
            }
        */
        JsonObject scheduleTypeObj = (JsonObject)json.get("scheduletype"); 
        
         /*
            "course": {
                "CS 304": {
                    "subjectid": "CS",
                    "num": "304",
                    "description": "Technical Writing for Computer Science (WI)",
                    "credits": 3
                },
                "CS 308": {
                    "subjectid": "CS",
                    "num": "308",
                    "description": "Embedded and Control Systems Security",
                    "credits": 3
                },
                "CS 310": {
                    "subjectid": "CS",
                    "num": "310",
                    "description": "Software Engineering I",
                    "credits": 3
                }
            }
        */
        JsonObject courseObj = (JsonObject)json.get("course");
        
        /*
            "subject": {
                "CS": "Computer Science",
                "EH": "English",
                "HY": "History",
                "MS": "Mathematics"
            }
        */
        JsonObject subjectObj = (JsonObject)json.get(SUBJECT_COL_HEADER); 
        
        /*
            "section": [
               {
                   "crn": 21095,
                   "subjectid": "CS",
                   "num": "304",
                   "section": "001",
                   "type": "LEC",
                   "start": "09:15:00",
                   "end": "10:45:00",
                   "days": "TR",
                   "where": "Ayers Hall 355",
                   "instructor": [
                       "Cynthia Gunter Jensen"
                   ]
               },
           ]
        */
        JsonArray sectionList = (JsonArray)json.get(SECTION_COL_HEADER);     
             
        List<String> csvRecord;
        
        List<String> header = List.of(CRN_COL_HEADER, SUBJECT_COL_HEADER, NUM_COL_HEADER,
                DESCRIPTION_COL_HEADER, SECTION_COL_HEADER, TYPE_COL_HEADER,
                CREDITS_COL_HEADER, START_COL_HEADER, END_COL_HEADER,
                DAYS_COL_HEADER, WHERE_COL_HEADER, SCHEDULE_COL_HEADER,
                INSTRUCTOR_COL_HEADER);
        
        StringWriter writer = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(writer, '\t', '"', '\\', "\n");

        // first goes in header
        csvWriter.writeNext(header.toArray(String[]::new));    
        
        for(int i = 0; i < sectionList.size(); i++){
            
            /*
                {
                    "crn": 21095,
                    "subjectid": "CS",
                    "num": "304",
                    "section": "001",
                    "type": "LEC",
                    "start": "09:15:00",
                    "end": "10:45:00",
                    "days": "TR",
                    "where": "Ayers Hall 355",
                    "instructor": [
                        "Cynthia Gunter Jensen"
                    ]
                }
            */
            JsonObject sectionItem = (JsonObject) sectionList.get(i); 
            
            HashMap<String, String> subjectObject = (HashMap)courseObj.get((sectionItem.get(SUBJECTID_COL_HEADER)+" "+sectionItem.get(NUM_COL_HEADER)));       
            
            // "crn" "subject" "num" "description" "section" "type" "credits" "start" "end" "days" "where" "schedule" "instructor"
            String crn = sectionItem.get(CRN_COL_HEADER).toString();
            String subject = (String) subjectObj.get(sectionItem.get(SUBJECTID_COL_HEADER));
            String num = sectionItem.get(SUBJECTID_COL_HEADER) + " " + sectionItem.get(NUM_COL_HEADER);
            String section = (String) sectionItem.get(SECTION_COL_HEADER);
            String type = (String) sectionItem.get(TYPE_COL_HEADER);
            String start = (String) sectionItem.get(START_COL_HEADER);
            String end = (String) sectionItem.get(END_COL_HEADER);
            String days = (String) sectionItem.get(DAYS_COL_HEADER);
            String where = (String) sectionItem.get(WHERE_COL_HEADER);
            String schedule = (String)scheduleTypeObj.get(sectionItem.get(TYPE_COL_HEADER));
                                                                              String instructor = (String) String.join(", ", (List) sectionItem.get(INSTRUCTOR_COL_HEADER));
            
            /*
                "CS 304": {
                    "subjectid": "CS",
                    "num": "304",
                    "description": "Technical Writing for Computer Science (WI)",
                    "credits": 3
                },
            */
            JsonObject course = (JsonObject) courseObj.get(num);
            String credits = course.get(CREDITS_COL_HEADER).toString();
            String description = (String) course.get(DESCRIPTION_COL_HEADER);
            
            // in correct order
            csvRecord = List.of(crn, subject, num, description, section, type, credits, start, end, days, where, schedule, instructor);
            
            csvWriter.writeNext(csvRecord.toArray(String[]::new));       
        
        }
        
        return writer.toString();    
        
    }
    
    public JsonObject getJson() {
        
        JsonObject json = getJson(getInputFileData(JSON_FILENAME));
        return json;
        
    }
    
    public JsonObject getJson(String input) {
        
        JsonObject json = null;
        
        try {
            json = (JsonObject)Jsoner.deserialize(input);
        }
        catch (Exception e) { e.printStackTrace(); }
        
        return json;
        
    }
    
    public List<String[]> getCsv() {
        
        List<String[]> csv = getCsv(getInputFileData(CSV_FILENAME));
        return csv;
        
    }
    
    public List<String[]> getCsv(String input) {
        
        List<String[]> csv = null;
        
        try {
            
            CSVReader reader = new CSVReaderBuilder(new StringReader(input)).withCSVParser(new CSVParserBuilder().withSeparator('\t').build()).build();
            csv = reader.readAll();
            
        }
        catch (Exception e) { e.printStackTrace(); }
        
        return csv;
        
    }
    
    public String getCsvString(List<String[]> csv) {
        
        StringWriter writer = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(writer, '\t', '"', '\\', "\n");
        
        csvWriter.writeAll(csv);
        
        return writer.toString();
        
    }
    
    private String getInputFileData(String filename) {
        
        StringBuilder buffer = new StringBuilder();
        String line;
        
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        
        try {
        
            BufferedReader reader = new BufferedReader(new InputStreamReader(loader.getResourceAsStream("resources" + File.separator + filename)));

            while((line = reader.readLine()) != null) {
                buffer.append(line).append('\n');
            }
            
        }
        catch (Exception e) { e.printStackTrace(); }
        
        return buffer.toString();
        
    }
    
}