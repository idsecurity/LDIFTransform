/* 
 * Copyright (C) 2017 almu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.idsecurity.LDIFTransform;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.EntrySorter;
import com.unboundid.ldap.sdk.ModificationType;
import com.unboundid.ldif.LDIFAddChangeRecord;
import com.unboundid.ldif.LDIFDeleteChangeRecord;
import com.unboundid.ldif.LDIFException;
import com.unboundid.ldif.LDIFReader;
import com.unboundid.ldif.LDIFRecord;
import com.unboundid.ldif.LDIFWriter;
import java.time.Duration;
import java.time.Instant;

/**
 * Transforms an LDIF file using a transformation file and a transformer class.
 *
 * @author almu
 * @since v1.0
 */
public class LDIFTransform {

    private static final int LENGTH_OF_ARGS = 5;

    private static final int SETSIZE = 30000;

    private static final Logger logger = LoggerFactory.getLogger(LDIFTransform.class);

    private static boolean noSort = false;
    
    /**
     * Quick way to measure the time taken to run this app
     * @since 1.5
     */
    private static Instant startTimeRead;
    
    /**
     * Quick way to measure the time taken to run this app
     * @since 1.5
     */
    private static final Instant loadTime = Instant.now();
    
    

    public static void main(String[] args) {

        if (args.length < LENGTH_OF_ARGS) {
            System.out.println("Usage: java -jar LDIFTransform-<version>.jar \n\t<path to transform file> \n\t<path to input LDIF> \n\t<path to output LDIF> \n\t<name of transformer class> \n\t<add|delete|modify-replace|modify-add|modify-delete|none> \n\t[NoSort]");
            System.out.println("\nTransformer class names:");

            for (Transformers t : Transformers.values()) {
                System.out.println(t);
            }

            System.exit(1);
        }

        if (args.length > 5 && args[5].equals("NoSort")) {
            noSort = true;
        }

        File transformProperties = new File(args[0]);
        logger.info("Transform properties file is: {}", transformProperties.getPath());

        File ldifInput = new File(args[1]);
        logger.info("LDIF input file is: {}", ldifInput.getPath());

        File ldifOutput = new File(args[2]);
        logger.info("LDIF output file is: {}", ldifOutput.getPath());

        String transformerClassName = args[3];
        logger.info("Transformer class is: {}", transformerClassName);

        String changeType = args[4];
        logger.info("Change type is: {}", changeType);
        
        logger.info("NoSort is: {}", noSort);

        TransformerCommon classToUse = GetTransformer.getTransformerClass(transformerClassName, transformProperties);

        LDIFReader reader = null;
        LDIFWriter writer = null;
        try {
            startTimeRead = Instant.now();
            
            int availableProcessors = Runtime.getRuntime().availableProcessors();
            if (availableProcessors >= 4) {
                availableProcessors = 4;
            }
            
            reader = new LDIFReader(new BufferedReader(new FileReader(ldifInput)), 0, classToUse);
            int entriesRead = 0;
            int errorsEncountered = 0;
            writer = new LDIFWriter(ldifOutput);
            writer.writeVersionHeader();

            Set<Entry> unsorted = new HashSet<>(SETSIZE);

            while (true) {
                Entry entry;
                try {
                    entry = reader.readEntry();
                    if (entry == null) {
                        logger.info("All entries processed: {}", entriesRead);
                        Instant endTimeRead = Instant.now();
                        logger.info("Read/translation time: {}", Duration.between(startTimeRead, endTimeRead));
                        break;
                    }
                    entriesRead++;

                    LDIFRecord record;
                    switch (changeType) {
                        case "add":
                            record = new LDIFAddChangeRecord(entry);
                            break;
                        case "delete":
                            record = new LDIFDeleteChangeRecord(entry.getDN());
                            break;
                        case "none":
                            record = entry;
                            break;
                        case "modify-add":
                            record = LDIFModifyGenerator.getModify(entry, ModificationType.ADD);
                            break;
                        case "modify-replace":
                            record = LDIFModifyGenerator.getModify(entry, ModificationType.REPLACE);
                            break;
                        case "modify-delete":
                            record = LDIFModifyGenerator.getModify(entry, ModificationType.DELETE);
                            break;
                        default:
                            record = new LDIFAddChangeRecord(entry);
                            break;
                    }
                    
                    //Skip records whose DN is "ignore"
                    if (!record.getDN().equalsIgnoreCase("ignore")) {
                        if (noSort) {
                            writer.writeLDIFRecord(record);
                        } else {
                            unsorted.add(entry);
                        }
                    }
                    

                } catch (LDIFException e) {
                    logger.error("Exception occured reading LDIF file", e);
                    errorsEncountered++;
                    if (e.mayContinueReading()) {
                        continue;
                    } else {
                        break;
                    }
                }
            }

            try {
                if (!noSort) {
                    EntrySorter sorter = new EntrySorter();
                    SortedSet<Entry> sorted = sorter.sort(unsorted);
                    for (Entry entry : sorted) {
                        LDIFRecord record;
                        switch (changeType) {
                            case "add":
                                record = new LDIFAddChangeRecord(entry);
                                break;
                            case "delete":
                                record = new LDIFDeleteChangeRecord(entry.getDN());
                                break;
                            case "modify-add":
                                record = LDIFModifyGenerator.getModify(entry, ModificationType.ADD);
                                break;
                            case "modify-replace":
                                record = LDIFModifyGenerator.getModify(entry, ModificationType.REPLACE);
                                break;
                            case "modify-delete":
                                record = LDIFModifyGenerator.getModify(entry, ModificationType.DELETE);
                                break;
                            case "none":
                                record = entry;
                                break;
                            default:
                                record = new LDIFAddChangeRecord(entry);
                                break;
                        }
                        //Skip records whose DN is "ignore"
                        if (!record.getDN().equalsIgnoreCase("ignore")) {
                            writer.writeLDIFRecord(record);
                        }
                        
                    }
                }

                for (LDIFRecord record : classToUse.getPostProcessingRecords()) {
                    writer.writeLDIFRecord(record);
                }

            } catch (IOException e) {
                logger.error("Exception occured writing LDIF file", e);
            }

        } catch (IOException e) {
            logger.error("Exception", e);
        } finally {
            try {
                reader.close();

            } catch (IOException ignored) {

            }

            try {
                writer.close();
            } catch (IOException ignored) {

            }
            Instant endTimeAll = Instant.now();
            logger.info("Time since read/translation began: {}", Duration.between(startTimeRead, endTimeAll));
            logger.info("Time since app was started: {}", Duration.between(loadTime, endTimeAll));
            
        }

    }

}
