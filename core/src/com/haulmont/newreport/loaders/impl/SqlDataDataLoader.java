package com.haulmont.newreport.loaders.impl;

import com.haulmont.newreport.exception.DataLoadingException;
import com.haulmont.newreport.structure.impl.Band;
import com.haulmont.newreport.structure.DataSet;
import com.haulmont.newreport.util.db.QueryRunner;
import com.haulmont.newreport.util.db.ResultSetHandler;
import org.apache.commons.lang.StringUtils;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author degtyarjov
 * @version $Id: SqlDataDataLoader.java 9930 2012-12-13 22:31:09Z artamonov $
 */
public class SqlDataDataLoader extends AbstractDbDataLoader {
    private DataSource dataSource;

    public SqlDataDataLoader(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Map<String, Object>> loadData(DataSet dataSet, Band parentBand, Map<String, Object> params) {
        List resList;
        final List<String> outputParameters = new ArrayList<String>();

        String query = dataSet.getScript();
        if (StringUtils.isBlank(query)) {
            return Collections.emptyList();
        }

        QueryRunner runner = new QueryRunner(dataSource);
        try {
            QueryPack pack = prepareNativeQuery(query, parentBand, params);

            resList = runner.query(pack.getQuery(), pack.getParams(), new ResultSetHandler<List>() {
                @Override
                public List handle(ResultSet rs) throws SQLException {
                    List<Object[]> resList = new ArrayList<Object[]>();

                    while (rs.next()) {
                        ResultSetMetaData metaData = rs.getMetaData();
                        if (outputParameters.size() == 0) {
                            for (int columnIndex = 1; columnIndex <= metaData.getColumnCount(); columnIndex++) {
                                String columnName = metaData.getColumnName(columnIndex);
                                outputParameters.add(columnName);
                            }
                        }
                        Object[] values = new Object[metaData.getColumnCount()];
                        for (int columnIndex = 0; columnIndex < metaData.getColumnCount(); columnIndex++) {
                            values[columnIndex] = convertOutputValue(rs.getObject(columnIndex + 1));
                        }
                        resList.add(values);
                    }

                    return resList;
                }
            });
        } catch (SQLException e) {
            throw new DataLoadingException(String.format("An error occurred while loading data for data set [%s]", dataSet.getName()), e);
        }

        return fillOutputData(resList, outputParameters);
    }

    protected QueryPack prepareNativeQuery(String query, Band parentBand, Map<String, Object> reportParams) throws SQLException {
        Map<String, Object> currentParams = new HashMap<String, Object>();
        if (reportParams != null) {
            currentParams.putAll(reportParams);
        }

        //adds parameters from parent bands hierarchy
        while (parentBand != null) {
            addParentBandDataToParameters(parentBand, currentParams);
            parentBand = parentBand.getParentBand();
        }

        List<ParamPosition> paramPositions = new ArrayList<ParamPosition>();
        List<Object> values = new ArrayList<Object>();
        for (Map.Entry<String, Object> entry : currentParams.entrySet()) {
            // Remembers ${alias} positions
            String paramName = entry.getKey();
            Object paramValue = entry.getValue();

            String alias = "${" + paramName + "}";
            String paramNameRegexp = "\\$\\{" + paramName + "\\}";

            String deleteRegexp = "(?i) *(and|or)? +[\\w|\\d|\\.|\\_]+ +(=|>=|<=|like) *" + paramNameRegexp;

            //if value == null - removing condition from query
            if (paramValue == null) {
                // remove unused null parameter
                query = query.replaceAll(deleteRegexp, "");
            } else if (query.contains(alias)) {
                Pattern pattern = Pattern.compile(paramNameRegexp);
                Matcher matcher = pattern.matcher(query);

                int subPosition = 0;
                while (matcher.find(subPosition)) {
                    paramPositions.add(new ParamPosition(paramNameRegexp, matcher.start(), convertParameter(paramValue)));
                    subPosition = matcher.end();
                }
            }
        }

        // Sort params by position
        Collections.sort(paramPositions, new Comparator<ParamPosition>() {
            @Override
            public int compare(ParamPosition o1, ParamPosition o2) {
                return o1.getPosition().compareTo(o2.getPosition());
            }
        });

        for (ParamPosition paramEntry : paramPositions) {
            // Replace all params by ?
            query = query.replaceAll(paramEntry.getParamRegexp(), "?");
            Object value = paramEntry.getValue();
            values.add(value);
        }

        query = query.trim();
        if (query.endsWith("where")) {
            query = query.replace("where", "");
        }

        return new QueryPack(query, values.toArray());
    }

    private static class ParamPosition {
        private Integer position;
        private Object value;
        private String paramRegexp;

        private ParamPosition(String paramRegexp, Integer position, Object value) {
            this.position = position;
            this.value = value;
            this.paramRegexp = paramRegexp;
        }

        public Integer getPosition() {
            return position;
        }

        public Object getValue() {
            return value;
        }

        public String getParamRegexp() {
            return paramRegexp;
        }
    }
}