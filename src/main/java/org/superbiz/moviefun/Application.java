package org.superbiz.moviefun;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.superbiz.moviefun.albums.AlbumsBean;
import org.superbiz.moviefun.movies.MoviesBean;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }

    @Bean
    public DatabaseServiceCredentials databaseServiceCredentials(@Value( "${VCAP_SERVICES}") String service) {
        return new DatabaseServiceCredentials(service);
    }

    @Bean(name="albumDS")
    public DataSource albumsDataSource(DatabaseServiceCredentials databaseServiceCredentials) {
        //DataSource ds = DataSourceBuilder.create().url(databaseServiceCredentials.jdbcUrl("albums-mysql")).build();
        HikariConfig hc = new HikariConfig();
        hc.setJdbcUrl(databaseServiceCredentials.jdbcUrl("albums-mysql"));
        return new HikariDataSource(hc);
    }

    @Bean
    public DataSource moviesDataSource(DatabaseServiceCredentials serviceCredentials) {
        //MysqlDataSource dataSource = new MysqlDataSource();
        //dataSource.setURL(serviceCredentials.jdbcUrl("movies-mysql"));
        HikariConfig hc = new HikariConfig();
        hc.setJdbcUrl(serviceCredentials.jdbcUrl("movies-mysql"));
        return new HikariDataSource(hc);
    }

    @Bean
    public HibernateJpaVendorAdapter hibernateJpaVendorAdapter() {

        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        hibernateJpaVendorAdapter.setGenerateDdl(true);
        hibernateJpaVendorAdapter.setDatabase(Database.MYSQL);
        hibernateJpaVendorAdapter.setShowSql(true);
        return hibernateJpaVendorAdapter;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean movies(HibernateJpaVendorAdapter hibernateJpaVendorAdapter, DataSource moviesDataSource) {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        factory.setPackagesToScan(MoviesBean.class.getPackage().getName());
        factory.setDataSource(moviesDataSource);
        factory.setPersistenceUnitName("movies");
        return factory;
    }


    @Bean
    public LocalContainerEntityManagerFactoryBean albums(HibernateJpaVendorAdapter hibernateJpaVendorAdapter, @Qualifier("albumDS") DataSource albumDataSource) {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        factory.setPackagesToScan(AlbumsBean.class.getPackage().getName());
        factory.setDataSource(albumDataSource);
        factory.setPersistenceUnitName("albums");
        return factory;
    }

    @Bean
    public PlatformTransactionManager moviesPTM(EntityManagerFactory movies) {
        return new JpaTransactionManager(movies);
    }

    @Bean
    public PlatformTransactionManager albumsPTM(EntityManagerFactory albums) {
        return new JpaTransactionManager(albums);
    }

}
