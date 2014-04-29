<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="ru">
    <head>
        <title> Таблица </title>
    </head>
    <body>
        <#assign Table1 = Root.bands.Band1>
        <#if Root.fields.date?has_content>${Root.fields.date?string("dd/MM/yyyy")}</#if>
        <#if Root.fields.date2?has_content>${Root.fields.date2?string("dd/MM/yyyy")}</#if>
        <table border="1" cellpadding="5" cellspacing="0" width="200">
            <thead>
                <tr>
                <td> Column1 </td>
                <td> Column2 </td>
                </tr>
            </thead>
            <tbody>
            <#list Table1 as row>
                <tr>
                    <td>
                        ${row.fields.col1}
                    </td>
                    <td>
                        ${row.fields.col2}
                    </td>
                    <td>
                        ${row.fields('col.nestedCol')}
                    </td>
                    <td>
                        ${row.fields('col.nestedBool')?string!}
                    </td>
                </tr>
            </#list>
            </tbody>
        </table>
    </body>
</html>