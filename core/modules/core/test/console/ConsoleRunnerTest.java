/*
 * Copyright 2014 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package console;

import com.haulmont.yarg.console.ConsoleRunner;
import org.junit.Test;

public class ConsoleRunnerTest {
    @Test
    public void testConsoleReport() throws Exception {
        ConsoleRunner.doExitWhenFinished = false;
        ConsoleRunner.main(new String[]{
                "-" + ConsoleRunner.REPORT_PATH, "./modules/core/test/console/console.xml",
                "-" + ConsoleRunner.OUTPUT_PATH, "./result/console/console.xls",
                "-Pparam1=param1",
                "-Pparam2=12/12/12 12:12",
                "-Pparam3=10",
                "-Pparam4=[{\"col1\":\"json1\",\"col2\":\"json2\"},{\"col1\":\"json3\",\"col2\":\"json4\"}]"
        });

    }
}
