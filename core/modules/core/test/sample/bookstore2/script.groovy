package sample.bookstore2.bookstore

import com.google.common.collect.ArrayListMultimap
import com.haulmont.yarg.console.DatasourceHolder
import com.haulmont.yarg.util.db.QueryRunner
import com.haulmont.yarg.util.db.ResultSetHandler

import java.sql.ResultSet
import java.sql.SQLException

QueryRunner runner = new QueryRunner(DatasourceHolder.dataSource);
def query =
        'select shop.id as "id", shop.name as "name", shop.address as "address", ' +
                '           book.author as "author", book.name as "name", book.price as "price", count(*) as "count" ' +
                'from shop, book book ' +
                'where book.store_id = shop.id ' +
                'group by shop.id, shop.name, shop.address book.author, book.name, book.price';
def shops = new HashSet()
def booksByShopId = ArrayListMultimap.create()

runner.query(query, new ResultSetHandler() {
    @Override
    Object handle(ResultSet rs) throws SQLException {
        while (rs.next()) {
            def shop = [:]
            shop['id'] = rs.getInt(1)
            shop['name'] = rs.getString(2)
            shop['address'] = rs.getString(3)

            shops.add(shop)
            def book = [:]
            book['author'] = rs.getString(4)
            book['name'] = rs.getString(5)
            book['price'] = rs.getBigDecimal(6)
            book['count'] = rs.getLong(7)

            booksByShopId.put(rs.getInt(1), book)
        }
        return null
    }
})

params['Shops'] = shops
params['Books'] = booksByShopId

return [[:]]