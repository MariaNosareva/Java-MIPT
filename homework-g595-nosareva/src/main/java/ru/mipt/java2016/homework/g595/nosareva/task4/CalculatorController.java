package ru.mipt.java2016.homework.g595.nosareva.task4;

import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import ru.mipt.java2016.homework.base.task1.Calculator;
import ru.mipt.java2016.homework.base.task1.ParsingException;
import ru.mipt.java2016.homework.g595.nosareva.task1.CalculatorAlpha;
import ru.mipt.java2016.homework.g595.nosareva.task1.CalculatorInterface;
import ru.mipt.java2016.homework.g595.nosareva.task1.Evaluator;
import ru.mipt.java2016.homework.g595.nosareva.task1.WCalculator;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

@RestController
public class CalculatorController {
    private static final Logger LOG = LoggerFactory.getLogger(CalculatorController.class);
    private HashMap<String, CalculatorInterface> usersClaculators = new HashMap<>();
    private ArrayList<String> preFunc = new ArrayList<>();

    public CalculatorController() {
        preFunc.add("sin");
        preFunc.add("cos");
        preFunc.add("tg");
        preFunc.add("sqrt");
        preFunc.add("min");
        preFunc.add("max");
        preFunc.add("pow");
        preFunc.add("abs");
        preFunc.add("sign");
        preFunc.add("log");
        preFunc.add("log2");
        preFunc.add("rnd");
    }

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private BillingDao billingDao;

    boolean load(String username) throws ParsingException, Exception {
        try {
            BillingUser user = billingDao.loadUser(username);
            if (!usersClaculators.containsKey(username)) {
                loadUser(username);
            }
            return true;
        } catch (EmptyResultDataAccessException e) {
            LOG.warn("No such user: " + username);
            return false;
        }
    }

    void loadUser(String username) throws ParsingException, Exception {
        ArrayList<String> functionsNames = billingDao.loadFunctions(username);
        ArrayList<String> variablesNames = billingDao.loadVariables(username);
        CalculatorInterface calculator = new WCalculator();
        HashMap<String, Evaluator> functionsMap = new HashMap<>();
        HashMap<String, Double> variablesMap = new HashMap<>();

        for (String functionName : functionsNames) {
            CalculatorAlpha func = billingDao.loadFunction(username, functionName);
            func.setFuncTab(functionsMap);
            func.setVarTab(variablesMap);
            functionsMap.put(functionName, func);
            calculator.insertFunction(functionName, func.getParameters(), func.getBody());
        }

        for (String variableName : variablesNames) {
            Double value = billingDao.loadVariable(username, variableName);
            variablesMap.put(variableName, value);
            calculator.insertVariable(variableName, value.toString());
        }
        usersClaculators.put(username, calculator);
    }

    @RequestMapping(path = "/ping", method = RequestMethod.POST, produces = "text/plain")
    public String echo(@RequestParam("value") String name) {
        return name + '\n';
    }

    @RequestMapping(path = "/", method = RequestMethod.GET, produces = "text/html")
    public String main(@RequestParam(required = false) String name) {
        if (name == null) {
            name = "world";
        }
        return "<html>" +
                "<head><title>MyApp</title></head>" +
                "<body><h1>Hello, " + name + "!</h1></body>" +
                "</html>";
    }

    @RequestMapping(path = "/variable/{variableName}", method = RequestMethod.GET)
    public Double varGET(@PathVariable String variableName) throws ParsingException, Exception {
        load(request.getUserPrincipal().getName());
        return usersClaculators.get(request.getUserPrincipal().getName()).getVariable(variableName);
    }


    @RequestMapping(path = "/variable/", method = RequestMethod.GET)
    public String varGETAll() throws Exception {
        load(request.getUserPrincipal().getName());
        return usersClaculators.get(request.getUserPrincipal().getName()).getListOfVariables().toString() + "\n";
    }

    @RequestMapping(path = "/variable/{variableName}", method = RequestMethod.PUT)
    public boolean varPUT(@PathVariable String variableName, @RequestBody String value) throws Exception{
        boolean ok = load(request.getUserPrincipal().getName());

        if (usersClaculators.containsKey(request.getUserPrincipal().getName())) {
            Double answer = usersClaculators.get(
                    request.getUserPrincipal().getName()).insertVariable(variableName, value);
            billingDao.insertVariable(request.getUserPrincipal().getName(), variableName, answer);
        }
        return ok;
    }

    @RequestMapping(path = "/variable/{variableName}", method = RequestMethod.DELETE)
    public boolean varDELETE(@PathVariable String variableName) throws Exception {
        boolean ok = load(request.getUserPrincipal().getName());
        boolean deleted = false;
        if (ok) {
            usersClaculators.get(request.getUserPrincipal().getName()).deleteVariable(variableName);
        }
        if (deleted) {
            billingDao.deleteVariable(request.getUserPrincipal().getName(), variableName);
        }
        return deleted && ok;
    }

    @RequestMapping(path = "/function/{functionName}", method = RequestMethod.GET)
    public String funcGET(@PathVariable String functionName) throws Exception {
        load(request.getUserPrincipal().getName());
        Pair<ArrayList<String>, String> list = usersClaculators.get(
                request.getUserPrincipal().getName()).getFunctionDefinition(functionName);
        StringBuilder result = new StringBuilder();
        result.append(functionName + "(");
        for (int i = 0; i < list.getKey().size(); i++) {
            result.append(list.getKey().get(i));
        }
        result.append(") = " + list.getValue().substring(0, list.getValue().length()) + "\n");
        return result.toString();
    }

    @RequestMapping(path = "/function/", method = RequestMethod.GET)
    public String funcGETAll() throws Exception {
        load(request.getUserPrincipal().getName());
        return usersClaculators.get(request.getUserPrincipal().getName()).getListOfFunctions().toString() + "\n";
    }

    @RequestMapping(path = "/function/{functionName}", method = RequestMethod.PUT)
    public boolean funcPUT(@PathVariable String functionName,
                           @RequestParam(value = "args") ArrayList<String> argumentsList,
                           @RequestBody String body) throws Exception{
        boolean ok = load(request.getUserPrincipal().getName());

        if (preFunc.contains(functionName)) {
            return false;
        }

        if (usersClaculators.containsKey(request.getUserPrincipal().getName())) {
            usersClaculators.get(request.getUserPrincipal().getName()).insertFunction(functionName, argumentsList, body);
            billingDao.insertFunction(request.getUserPrincipal().getName(), functionName, argumentsList, body);
        }
        return ok;
    }

    @RequestMapping(path = "/function/{functionName}", method = RequestMethod.DELETE)
    public boolean funcDELETE(@PathVariable String functionName) throws Exception {
        boolean ok = load(request.getUserPrincipal().getName());
        boolean deleted = false;
        if (ok) {
            usersClaculators.get(request.getUserPrincipal().getName()).deleteFunction(functionName);
        }
        if (deleted) {
            billingDao.deleteFunction(request.getUserPrincipal().getName(), functionName);
        }
        return deleted && ok;
    }

    @RequestMapping(path = "/eval", method = RequestMethod.POST, consumes = "text/plain", produces = "text/plain")
    public String eval(@RequestBody String expression) throws Exception {
        load(request.getUserPrincipal().getName());
        LOG.debug("Evaluation request: [" + expression + "]");
        double result = usersClaculators.get(request.getUserPrincipal().getName()).evaluate(expression);
        LOG.trace("Result: " + result);
        return Double.toString(result) + "\n";
    }

    @RequestMapping(path = "/register", method = RequestMethod.PUT)
    public boolean registerUser(@RequestParam(value = "username") String username,
                                     @RequestParam(value = "password") String password,
                                     @RequestParam(value = "confirmPassword") String password2) {
        boolean res = false;
        try {
            billingDao.loadUser(username);
            res = true;
        } catch (EmptyResultDataAccessException exc) {
            if (password.equals(password2)) {
                billingDao.insertUser(username, password);
                res = true;
            } else {
                res = false;
            }
        }
        return res;
    }
}
