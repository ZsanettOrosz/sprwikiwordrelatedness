/* Copyright 2010 Speech and Language Technologies Lab, The Ohio State University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.osu.slate.relatedness.swwr.setup.wordmapping;

import java.io.*;
import java.util.*;

import com.aliasi.tokenizer.LineTokenizerFactory;
import com.aliasi.tokenizer.PorterStemmerTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;

import edu.osu.slate.relatedness.swwr.data.mapping.*;

/**
 * Turns temporary (String, ID) file into Word-ID and ID-Word Mappings.
 * <p>
 * Requires the output of {@link CreateTitleWordMapping} or {@link CreateLinkWordMapping}.  It also requires that <a href="http://alias-i.com/lingpipe/">LingPipe</a> be installed and the jar file accessible to the java environment.
 * <p>
 * If desired, stemming for more robust mapping functions may be applied to the words here.
 * <p>
 * Configuration File Requirements:
 * <ul>
 * <li><b>basedir</b> -- base directory for the files </li>
 * <li><b>sourcedir</b> -- raw sql/xml data file directory (default: source)</li>
 * <li><b>binarydir</b> -- generated binary file directory (default: binary)</li>
 * <li><b>tempdir</b> -- directory to store temporary files (default: tmp)</li>
 * <li><b>type</b> -- type of wiki to read (enwiki or enwiktionary)</li>
 * <li><b>date</b> -- date of wiki dump</li>
 * <li><b>graph</b> -- graph source information</li>
 * <li><b>stem</b> -- Allow Porter Stemming? (default: false)</li>
 * </ul>
 * 
 * The output of this program is a <i>.wic file</i> and an <i>.iwc file</i> placed in the binary directory.  These files will be used a input files for the {@link IDToWordMapping} and {@link WordToIDMapping} classes.
 * 
 * @author weale
 *
 */
public class CreateMappings {

  private static String baseDir, sourceDir, binaryDir, tempDir;
  private static String type, date, graph, stemChar;
  private static boolean stem;
  private static PorterStemmerTokenizerFactory porter;
  private static LineTokenizerFactory tokens;

 /**
  * Parse the configuration file.
  *  
  * @param filename Configuration file name.
  */ 
  private static void parseConfigurationFile(String filename) {
    try {
      Scanner config = new Scanner(new FileReader(filename));
      
      // Defaults
      sourceDir = "source";
      binaryDir = "binary";
      tempDir = "tmp";
      stem = false;
      stemChar = "f";
      
      while(config.hasNext()) {
        String s = config.nextLine();
        
        if(s.contains("<basedir>"))
        {
          baseDir = s.substring(s.indexOf("<basedir>") + 9,
                                s.indexOf("</basedir>"));
        }
        else if(s.contains("<sourcedir>"))
        {
          sourceDir = s.substring(s.indexOf("<sourcedir>") + 11,
                                  s.indexOf("</sourcedir>"));          
        }
        else if(s.contains("<binarydir>"))
        {
          binaryDir = s.substring(s.indexOf("<binarydir>") + 11,
                                  s.indexOf("</binarydir>"));
        }
        else if(s.contains("<tempdir>"))
        {
          tempDir = s.substring(s.indexOf("<tempdir>") + 9,
                                s.indexOf("</tempdir>"));
        }
        else if(s.contains("<type>"))
        {
          type = s.substring(s.indexOf("<type>") + 6,
                             s.indexOf("</type>"));
        }
        else if(s.contains("<date>"))
        {
          date = s.substring(s.indexOf("<date>") + 6,
                             s.indexOf("</date>"));
        }
        else if(s.contains("<graph>"))
        {
          graph = s.substring(s.indexOf("<graph>") + 7,
                              s.indexOf("</graph>"));
        }
        else if(s.contains("<stem>"))
        {
          stem = s.substring(s.indexOf("<stem>") + 6,
                             s.indexOf("</stem>")).equals("true");
        }
      }//end: while(config)
    }//end: try{}
    catch (IOException e)
    {
      System.err.println("Problem reading from file: " + filename);
      System.exit(1);
    }
  }//end: parseConfigurationFile()
  
  /**
   * Creates a final word-to-vertex mapping from the (word,id) pairs from a wiki data source.
   *
   * @param args
   * @throws IOException 
   * @throws FileNotFoundException 
   * @throws ClassNotFoundException 
   */
  public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {
    
    parseConfigurationFile("/scratch/weale/data/config/enwiktionary/CreateMappings.xml");

    System.out.println("Opening Temporary File for Reading");
    ObjectInputStream in = new ObjectInputStream(new FileInputStream(baseDir + "/" + tempDir + "/" + type + "-"+ date + "-" + graph + ".titlewordmap"));
    
    if(stem) {
      //porter = new PorterStemmerTokenizerFactory(tokens);
      stemChar = "t";
    }
    
   /* STEP 1
    * 
    * Create the word set in order to know how many words
    * are in our initial file.
    */
    System.out.println("Creating Word Array");
    TreeSet<String> ts = new TreeSet<String>();
    try {
      while(true) {
        String s = (String) in.readObject();
        if(stem) {
          ts.add(PorterStemmerTokenizerFactory.stem(s));
        }
        else
        {
          ts.add(s);
        }
        
        in.readInt();
      }//end: while(true)
    }//end: try{} 
    catch(IOException e) {} //EOF found
    
   /* STEP 2
    * 
    * Create the WordToIDCount array of the appropriate size
    * and initialize objects.
    * 
    * We use arrays for space efficiency -- these can get large.
    */
    WordToIDCount[] words = new WordToIDCount[ts.size()];
    Iterator<String> set = ts.iterator();
    int i=0;
    while(set.hasNext()){
      words[i] = new WordToIDCount(set.next());
      i++;
    }//end: while(set)
    Arrays.sort(words, new WordToIDCountComparator());
    ts = null; // Free Up Memory (?)
    in.close();
    
   /* STEP 3
    *  
    * Add IDs to our WordToIDCount objects.
    */
    System.out.println("Creating Vertex Mappings");
    in = new ObjectInputStream(new FileInputStream(baseDir + "/" + tempDir + "/" + type + "-"+ date + "-" + graph + ".titlewordmap"));
    try {
      while(true) {
        String word = (String) in.readObject();
        int vertex = in.readInt();
        
        if(stem) {
           word = porter.stem(word);
        }
        
        int pos = Arrays.binarySearch(words, new WordToIDCount(word), new WordToIDCountComparator());
        if(pos >= 0) {
          words[pos].addID(vertex);
        } else {
          System.err.println("Invalid Word Found: "+ word);
        }
      }//end: while(true)
    }//end: try {}
    catch(IOException e) {} //EOF found
    
    in.close();
    
   /* STEP 4
    * 
    * Write objects to the .wic file
    */
    ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(baseDir + "/" + binaryDir + "/" + type+ "/" + date + "/" + type + "-"+ date + "-" + graph + "-" + stemChar + ".wic"));
    out.writeInt(words.length);
    for(int x = 0; x < words.length; x++) {
      out.writeObject(words[x]);
    }
    out.close();
    words = null; // Free Up Memory (?)
    
    /* STEP 5
     * 
     * Create the id set in order to know how many ids
     * are in our initial file.
     */
    in = new ObjectInputStream(new FileInputStream(baseDir + "/" + tempDir + "/" + type + "-"+ date + "-" + graph + ".titlewordmap"));

    System.out.println("Creating Integer Array");
    TreeSet<Integer> ts2 = new TreeSet<Integer>();
    try {
      while(true) {
        in.readObject();
        ts2.add(in.readInt());
      }//end: while(true)
    }
    catch(IOException e) {} //EOF found

   /* STEP 6
    * 
    * Create the IDToWordCount array of the appropriate size
    * and initialize objects.
    * 
    * We use arrays for space efficiency -- these can get large.
    */ 
    IDToWordCount[] ids = new IDToWordCount[ts2.size()];
    Iterator<Integer> it = ts2.iterator();
    i=0;
    while(it.hasNext()){
      ids[i] = new IDToWordCount(it.next());
      i++;
    }//end: while(set)
    Arrays.sort(ids, new IDToWordCountComparator());
    ts2 = null; // Free Up Memory (?)
    in.close();
    
   /* STEP 7
    *  
    * Add words to our IDToWordCount objects.
    */
    System.out.println("Creating Word Mappings");
    in = new ObjectInputStream(new FileInputStream(baseDir + "/" + tempDir + "/" + type + "-"+ date + "-" + graph + ".titlewordmap"));
    try {
      while(true) {
        String word = (String) in.readObject();
        int vertex = in.readInt();
        
        int pos = Arrays.binarySearch(ids, new IDToWordCount(vertex), new IDToWordCountComparator());
        if(pos >= 0) {
          ids[pos].addWord(word);
        } else {
          System.err.println("Invalid ID Found: "+ vertex);
        }
      }//end: while(true)
    }
    catch(IOException e) {} //EOF found
    
    in.close();
    
    /* STEP 8
     * 
     * Write objects to the .iwc file
     */
    out = new ObjectOutputStream(new FileOutputStream(baseDir + "/" + binaryDir + "/" + type+ "/" + date + "/" + type + "-"+ date + "-" + graph + "-" + stemChar + ".iwc"));
    out.writeInt(ids.length);
    for(int x = 0; x < ids.length; x++) {
      out.writeObject(ids[x]);
    }
    out.close();
    ids = null; // Free Up Memory (?)
    
  }//end: main()

}
