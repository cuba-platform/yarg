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
 * @version $Id: SqlDataLoader.java 9930 2012-12-13 22:31:09Z artamonov $
 */
public class SqlDataLoader extends AbstractDbDataLoader {
    private DataSource dataSource;

    public SqlDataLoader(DataSource dataSource) {
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

        List<QueryParameter> queryParameters = new ArrayList<QueryParameter>();
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
                    queryParameters.add(new QueryParameter(paramNameRegexp, matcher.start(), convertParameter(paramValue)));
                    subPosition = matcher.end();
                }
            }
        }

        // Sort params by position
        Collections.sort(queryParameters, new Comparator<QueryParameter>() {
            @Override
            public int compare(QueryParameter o1, QueryParameter o2) {
                return o1.getPosition().compareTo(o2.getPosition());
            }
        });

        for (QueryParameter parameter : queryParameters) {
            if (parameter.isSingleValue()) {
                // Replace single parameter with ?
                query = query.replaceAll(parameter.getParamRegexp(), "?");
                Object value = parameter.getValue();
                values.add(value);
            } else {
                // Replace multiple parameter with ?,..(N)..,?
                List<?> multipleValues = parameter.getMultipleValues();
                StringBuilder builder = new StringBuilder();
                for (Object value : multipleValues) {
                    builder.append("?,");
                    values.add(value);
                }
                builder.deleteCharAt(builder.length() - 1);
                query = query.replaceAll(parameter.getParamRegexp(), builder.toString());
            }
        }

        query = query.trim();
        if (query.endsWith("where")) {
            query = query.replace("where", "");
        }

        return new QueryPack(query, values.toArray());
    }

    private static class QueryParameter {
        private Integer position;
        private Object value;
        private String paramRegexp;

        private QueryParameter(String paramRegexp, Integer position, Object value) {
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

        public boolean isSingleValue() {
            return !(value instanceof Collection || value instanceof Object[]);
        }

        public List<?> getMultipleValues() {
            if (isSingleValue()) {
                return Collections.singletonList(value);
            } else {
                if (value instanceof Collection) {
                    return new ArrayList<Object>((Collection<?>) value);
                } else if (value instanceof Object[]) {
                    return Arrays.asList((Object[]) value);
                }
            }

            return null;
        }
    }
}