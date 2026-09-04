package eionet.cr;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

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
