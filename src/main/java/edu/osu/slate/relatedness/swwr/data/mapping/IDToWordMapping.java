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

package edu.osu.slate.relatedness.swwr.data.mapping;

import java.io.*;
import java.util.Arrays;

/**
 * Simplified lookup class for the {@link IDToWordCount} class.
 * 
 * @author weale
 * @version 1.0
 */
public class IDToWordMapping {
  
  IDToWordCount[] ids;
  
  /**
   * Constructor.
   * <p>
   * Reads the {@link IDToWordCount} array from the given <i>.iwc file</i>.
   * 
  * @param filename Input file name.
  */
  public IDToWordMapping(String filename)
  {
    try {
      ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
      
      // Read array length
      int len = in.readInt();
      
      // Create and initialize array
      ids = new IDToWordCount[len];
      for(int i = 0; i < len; i++)
      {
          ids[i] = (IDToWordCount) in.readObject();
      }//end: for(i)
      
      in.close();
    }//end: try{}
    catch(IOException e)
    {
      System.err.println("Problem reading from file: " + filename);
      e.printStackTrace();
      System.exit(1);
    }
    catch (ClassNotFoundException e)
    {
      System.err.println("Problem with class conversion from file: " + filename);
      e.printStackTrace();
      System.exit(1);
    }
  }//end: IDToWordMapping()
  
 /**
  * Gets the Words Mapped to a given ID.
  * <p>
  * Returns null if ID is not found.
  *  
  * @param id ID to be Mapped.
  * @return An array of {@link WordCount} objects.
  */
  public WordCount[] getWordMappings(int id) {
    int pos = Arrays.binarySearch(ids, new IDToWordCount(id), new IDToWordCountComparator());

    if(pos >= 0)
    { // FOUND!
      return ids[pos].getWordCounts();
    }
    
    return null;
  }//end: getWordMappings(int)
}
