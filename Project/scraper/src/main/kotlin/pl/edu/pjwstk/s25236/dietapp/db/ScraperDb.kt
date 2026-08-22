package pl.edu.pjwstk.s25236.dietapp.db

import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

object ScraperDb {
    val dataSource: DataSource =
        HikariDataSource().apply {
            this.jdbcUrl = "jdbc:postgresql://146.59.95.136:5432/diet_db"
            this.username = "scraper_user"
            this.password = "1234"
            this.maximumPoolSize = 3
            this.schema = "diet_app"
        }
}
