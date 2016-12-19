package ru.mipt.java2016.homework.g595.nosareva.task1;

import javafx.util.Pair;
import ru.mipt.java2016.homework.base.task1.Calculator;
import ru.mipt.java2016.homework.base.task1.ParsingException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by maria on 18.12.16.
 */
public class WCalculator implements CalculatorInterface {
    public static final CalculatorInterface INSTANCE = new WCalculator();
    private HashMap<String, Double> variables = new HashMap<>();
    private HashMap<String, Evaluator> functions = new HashMap<>();

    public WCalculator() {}

    public Double evaluate(String expr) throws ParsingException, Exception {
        return (new CalculatorAlpha(variables, functions, new ArrayList<>(), expr)).eval();
    }

    @Override
    public Double getVariable(String variableName) {
        Double value;
        if (variables.containsKey(variableName)) {
            value = variables.get(variableName);
        } else {
            value = null;
        }
        return value;
    }

    @Override
    public Double insertVariable(String variableName, String variableValue) throws ParsingException, Exception {
        CalculatorAlpha calculus = new CalculatorAlpha(variables, functions,
                new ArrayList<>(), variableValue);
        Double temp = calculus.eval();
        variables.put(variableName, temp);
        return temp;
    }

    @Override
    public boolean deleteVariable(String variableName) {
        boolean deleted = false;
        if (variables.containsKey(variableName)) {
            variables.remove(variableName);
            deleted = true;
        }
        return deleted;
    }

    @Override
    public ArrayList<String> getListOfVariables() {
        return new ArrayList<>(variables.keySet());
    }

    @Override
    public Pair<ArrayList<String>, String> getFunctionDefinition(String functionName) {
        if (!functions.containsKey(functionName)) {
            return null;
        }
        return new Pair<>(functions.get(functionName).getParameters(), functions.get(functionName).getBody());
    }

    @Override
    public void insertFunction(String functionName, ArrayList<String> params, String body) {
        functions.put(functionName, new CalculatorAlpha(variables, functions, params, body));
    }

    @Override
    public boolean deleteFunction(String functionName) {
        boolean deleted = false;
        if (functions.containsKey(functionName)) {
            functions.remove(functionName);
            deleted = true;
        }
        return deleted;
    }

    @Override
    public ArrayList<String> getListOfFunctions() {
        return new ArrayList<>(functions.keySet());
    }
}
