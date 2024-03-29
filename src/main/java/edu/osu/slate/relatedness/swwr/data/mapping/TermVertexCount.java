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

import java.io.IOException;
import java.io.Serializable;

/**
 * Triple of (term, vertex, count).
 * 
 * @author weale
 * @version 1.0
 */
public class TermVertexCount implements Serializable {
  
 /**
   * 
   */
  private static final long serialVersionUID = 7700956091903015001L;
  
 /**
  * 
  */
  private int vertex, count;
  
  private String term;
  
 /**
  * Constructor.
  * 
  * @param t Term for triple.
  * @param v Vertex for triple.
  * @param c Initial triple count.
  */
  public TermVertexCount(String t, int v, int c) {
    term = t;
    vertex = v;
    count = c;
  }
  
 /**
  * Gets the term for the (term, vertex, count) triple.
  * 
  * @return Term contained in this triple.
  */
  public String getTerm()
  {
    return term;
  }
  
 /**
  * Gets the vertex for the (term, vertex, count) triple.
  * 
  * @return Vertex contained in this triple.
  */
  public int getVertex()
  {
    return vertex;
  }
  
 /**
  * Gets the count for the (term, vertex, count) triple.
  * 
  * @return Count contained in this triple.
  */
  public int getCount()
  {
    return count;
  }
  
 /**
  * Sets a new count for the triple.
  * 
  * @param c New count.
  */
  public void setCount(int c)
  {
    count = c;
  }
  
 /**
  * Writes the individual objects to the stream.
  * 
  * @param out {@link ObjectOutputStream} to write to.
  * @throws IOException
  */
  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    out.writeObject(term);
    out.writeInt(vertex);
    out.writeInt(count);
  }
  
 /**
  * Reads the individual objects from the stream.
  *  
  * @param in {@link ObjectInputStream} to read from.
  * @throws IOException
  * @throws ClassNotFoundException
  */
  private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
    term = (String) in.readObject();
    vertex = in.readInt();
    count = in.readInt();
  }
}
