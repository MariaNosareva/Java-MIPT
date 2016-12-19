package ru.mipt.java2016.homework.g595.nosareva.task1;

import ru.mipt.java2016.homework.base.task1.ParsingException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by maria on 18.12.16.
 */
public interface Evaluator {
    default boolean ifPredefined() {
        return false;
    }

    void setArgs(ArrayList<Double> arguments) throws Exception;

    Double eval() throws ParsingException, Exception;

    ArrayList<String> getParameters();

    String getBody();

    void setFuncTab(HashMap<String, Evaluator> funcTab);

    void setVarTab(HashMap<String, Double> varTab);
}
