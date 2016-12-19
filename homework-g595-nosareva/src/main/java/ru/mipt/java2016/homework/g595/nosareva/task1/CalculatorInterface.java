package ru.mipt.java2016.homework.g595.nosareva.task1;

import javafx.util.Pair;
import ru.mipt.java2016.homework.base.task1.ParsingException;

import java.util.ArrayList;

/**
 * Created by maria on 18.12.16.
 */
public interface CalculatorInterface {

    // Evaluate an expression
    Double evaluate(String expression) throws ParsingException, Exception;

    Double getVariable(String variableName);

    Double insertVariable(String variableName, String variableValue) throws ParsingException, Exception;

    boolean deleteVariable(String variableName);

    ArrayList<String> getListOfVariables();

    Pair<ArrayList<String>, String> getFunctionDefinition(String functionName);

    void insertFunction(String functionName, ArrayList<String> params, String body);

    boolean deleteFunction(String functionName);

    ArrayList<String> getListOfFunctions();
}

