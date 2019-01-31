/*
 * Copyright 2013 Haulmont
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

package utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static com.google.common.base.Preconditions.checkNotNull;

public class FixtureUtils {

    public static void loadDb(DataSource ds, String file) throws IOException, SQLException, URISyntaxException {
        checkNotNull(ds);
        String sql = FileUtils.readFileToString(FileLoader.load(file), "UTF-8").replaceAll("--.*\r?\n+", "");
        if (StringUtils.isEmpty(sql)) return;
        try (Connection connection = ds.getConnection()) {
            connection.setAutoCommit(false);
            for (String q: sql.split("[\\^][\\s|\\t|\\n]*")) {
                if (StringUtils.isEmpty(q)) continue;
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate(q);
                } catch (SQLException e) {
                    connection.rollback();
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

}
