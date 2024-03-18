/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.github.costcoe.costcoe.web;

import org.apache.commons.csv.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CSVManager {

    private static final String CSV_FILE_PATH = System.getenv("CSV_FILE_PATH");
    private static final int max_open_ports = Integer.parseInt(System.getenv("MAX_OPEN_PORTS"));
    private static boolean[] takenPorts = new boolean[max_open_ports];
    private static final int start_port = Integer.parseInt(System.getenv("START_PORT"));

    public static void init(){
        try {
            File file = new File(CSV_FILE_PATH);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addSession(String sessionId, String nodeIp, String port) {
        try {
            File file = new File(CSV_FILE_PATH);
            CSVPrinter csvPrinter;
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            FileWriter fileWriter = new FileWriter(CSV_FILE_PATH, true);

            if (file.length() == 0){
                csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT.withHeader("SessionID", "NodeIP", "Port"));
            }
            else {
                csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT);
            }
            // Create a new record with the provided sessionId and userData
            csvPrinter.printRecord(Arrays.asList(sessionId, nodeIp, port));

            // Close the CSV printer
            csvPrinter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getNodeIp(String sessionId) {
        try {
            // Read all records from the CSV file
            List<CSVRecord> records = readCSVFile();
            // Find the record with the specified sessionId and get specified info
            for (CSVRecord record : records) {
                if (record.get("SessionID").equals(sessionId)) {
                    //return data once found
                    return record.toMap().get("NodeIP");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static String getPort(String sessionId) {
        try {
            // Read all records from the CSV file
            List<CSVRecord> records = readCSVFile();
            // Find the record with the specified sessionId and get specified info
            for (CSVRecord record : records) {
                if (record.get("SessionID").equals(sessionId)) {
                    //return data once found
                    return record.toMap().get("Port");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Method to update user data for a session in the CSV file
    public static void updateNode(String sessionId, String updatedNode) {
        try {
            // Read all records from the CSV file
            List<CSVRecord> records = readCSVFile();
            // Find the record with the specified sessionId and update its data
            for (CSVRecord record : records) {
                if (record.get("SessionID").equals(sessionId)) {
                    // Update the UserData column for the matching session
                    record.toMap().put("NodeIP", updatedNode);
                    break; // Stop searching once found
                }
            }

            // Write the updated records back to the CSV file
            writeCSVFile(records);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void updatePort(String sessionId, String updatedPort) {
        try {
            // Read all records from the CSV file
            List<CSVRecord> records = readCSVFile();
            // Find the record with the specified sessionId and update its data
            for (CSVRecord record : records) {
                if (record.get("SessionID").equals(sessionId)) {
                    // Update the UserData column for the matching session
                    record.toMap().put("Port", updatedPort);
                    break; // Stop searching once found
                }
            }

            // Write the updated records back to the CSV file
            writeCSVFile(records);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to delete a session entry from the CSV file
    public static void deleteSessionEntry(String sessionId) {
        try {
            // Read all records from the CSV file
            List<CSVRecord> records = readCSVFile();

            // Find the record with the specified sessionId and remove it
            for (int i = 0; i < records.size(); i++) {
                if (records.get(i).get("SessionID").equals(sessionId)) {
                    takenPorts[Integer.parseInt(records.get(i).get("Port")) - start_port - 1] = false;
                    records.remove(i);
                    break; // Stop searching once found
                }
            }

            // Write the updated records back to the CSV file
            writeCSVFile(records);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // finds the lowest available port
    public static String availablePort() throws OutOfPortsException{ 
        try {

            List<String> nodePorts = new ArrayList<>();

            // Read all records from the CSV file
            List<CSVRecord> records = readCSVFile();
           // compare all taken ports on given node
            for (CSVRecord record : records) {
                 nodePorts.add(record.get("Port"));
            }

            int lowestPort = -1;
            for (int i = 0; i < takenPorts.length; i++){
                if (!takenPorts[i]) {
                    takenPorts[i] = true;
                    lowestPort = i;
                    break;
                }
            }

            if (lowestPort == -1){
                throw new OutOfPortsException("Out of ports!!!!!!");
            }

            return Integer.toString(lowestPort + start_port);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Method to read all records from the CSV file
    private static List<CSVRecord> readCSVFile() throws IOException {
        try (Reader reader = new FileReader(CSV_FILE_PATH);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader("SessionID", "NodeIP", "Port"))) {
            
            return csvParser.getRecords();
        }
    }

    // Method to write records to the CSV file
    private static void writeCSVFile(List<CSVRecord> records) throws IOException {
        try (FileWriter fileWriter = new FileWriter(CSV_FILE_PATH);
             CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT)) {

            for (CSVRecord record : records) {
                csvPrinter.printRecord(record);
            }
        }
    }
}

class OutOfPortsException extends Exception{
    public OutOfPortsException() {}

    public OutOfPortsException(String message){
        super(message);
    }
}