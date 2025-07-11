/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package se.yolean.quarkus.mpxj.it;

import java.io.IOException;
import java.io.InputStream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.mpxj.MPXJException;
import org.mpxj.ProjectFile;
import org.mpxj.reader.ProjectReader;
import org.mpxj.reader.UniversalProjectReader;

@Path("/quarkus-mpxj")
@ApplicationScoped
public class QuarkusMpxjResource {
  // add some rest methods here

  @GET
  @Path("example-project")
  public String hello() throws IOException, MPXJException {
    try (InputStream is = getClass().getResourceAsStream("example-project.xml")) {
      assert is != null;

      ProjectReader reader = new UniversalProjectReader();
      try {
        ProjectFile file = reader.read(is);
      } catch (Exception e) {
        e.printStackTrace();
      }

      return "Success";
    }
  }
}
