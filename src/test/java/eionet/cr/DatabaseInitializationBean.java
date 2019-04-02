package eionet.cr;

import eionet.cr.util.TestUtils;
import eionet.cr.util.sesame.SesameUtil;
import org.junit.runner.RunWith;
import org.openrdf.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 *
 *
 */
@Component
//@RunWith(SpringRunner.class)
//@ContextConfiguration(classes = { ApplicationTestContext.class })
@DependsOn({"configurationPostProcessor", "dataSource", "springApplicationContext"})
public class DatabaseInitializationBean {

    @PostConstruct
    public void init() throws Exception {
/*        Resource rs = new ClassPathResource("create_users.sql");
        ScriptUtils.executeSqlScript( SesameUtil.getSQLConnection(), rs);*/
//        ResourceDatabasePopulator pop = new ResourceDatabasePopulator();
//        pop.
//        template.;
//        TestUtils.setUpDatabase(ds, "create_users.sql");
//        TestUtils.setUpDatabase(ds, "create_users_test.sql");
    }


}
