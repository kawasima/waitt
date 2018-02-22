package waitt.action;

import org.seasar.extension.jdbc.JdbcManager;
import org.seasar.struts.annotation.Execute;
import waitt.entity.Employee;

import javax.annotation.Resource;
import java.util.List;

public class IndexAction {
    @Resource
    private JdbcManager jdbcManager;

    @Execute(validator = false)
    public String index() {
        List<Employee> employees = jdbcManager
            .from(Employee.class)
            .getResultList();
        return "index.jsp";
    }
}
