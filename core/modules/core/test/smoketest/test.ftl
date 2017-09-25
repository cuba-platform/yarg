<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="ru">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
        <title> Таблица </title>
        <style>
        html *
        {
           font-size: 1em !important;
           color: #000 !important;
           font-family: Arial !important;
        }
        </style>
    </head>
    <body>
        <div style="display: table">
            <div>
                <img src="http://samara1.haulmont.com:8594/app/VAADIN/resources/img/Sherlock_logo.png" height="68" width="199" border="0" align="right"/>
                <!--<img src="file:///data/Sherlock_logo.png" height="68" width="199" border="0" align="right"/>-->
            </div>
            <div>
                <#assign Table1 = Root.bands.Band1>
                <#if Root.fields.date?has_content>${Root.fields.date?string("dd/MM/yyyy")}</#if>
                <#if Root.fields.date2?has_content>${Root.fields.date2?string("dd/MM/yyyy")}</#if>
                <p>Таблица</p>
                <table border="1" cellpadding="5" cellspacing="0" width="200">
                    <thead>
                        <tr>
                        <td> Колонка 1 </td>
                        <td> Колонка 2 </td>
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
            </div>
        </div>
    </body>
</html>