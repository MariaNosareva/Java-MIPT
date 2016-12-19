package ru.mipt.java2016.homework.g595.nosareva.task1;

import javafx.util.Pair;
import ru.mipt.java2016.homework.base.task1.ParsingException;
import ru.mipt.java2016.homework.base.task1.Calculator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CalculatorAlpha implements Evaluator {

    private int position;
    private String strToCalculate;

    private HashMap<String, Double> variablesTable;
    private HashMap<String, Evaluator> functionTable;

    // parameters
    private HashMap<Integer, String> fromNumberToPar = new HashMap<>();

    // put arguments
    private HashMap<String, Double> numberedParameters = new HashMap<>();

    public CalculatorAlpha(HashMap<String, Double> varTab,
                           HashMap<String, Evaluator> funcTab,
                           ArrayList<String> par,
                           String string) {
        strToCalculate = string;
        variablesTable = varTab;
        functionTable = funcTab;
        if (par != null) {
            for (int index = 0; index < par.size(); index++) {
                fromNumberToPar.put(index, par.get(index));
            }
        }
    }



    private void getEps() {
        while (strToCalculate.charAt(position) == ' ' ||
                strToCalculate.charAt(position) == '\n' ||
                strToCalculate.charAt(position) == '\t') {
            position++;
        }
    }

    private StringBuilder getNumber() {
        StringBuilder number = new StringBuilder();
        while (strToCalculate.charAt(position) >= '0' &&
                strToCalculate.charAt(position) <= '9') {
            number.append(strToCalculate.charAt(position++));
        }
        return number;
    }

    private String getPoint() {
        StringBuilder number = getNumber();
        if (strToCalculate.charAt(position) == '.') {
            position++;
            number.append('.');
            number.append(getNumber());
        }
        return number.toString();
    }

    private Pair<Boolean, Double> Predefined(String function, ArrayList<Double> args) throws ParsingException {
        switch (function) {
            case "sin":
                if (args.size() != 1) {
                    throw new ParsingException("Wrong number of arguments in sin()");
                }
                return new Pair<>(true, Math.sin(args.get(0)));
            case "cos":
                if (args.size() != 1) {
                    throw new ParsingException("Wrong number of arguments in cos()");
                }
                return new Pair<>(true, Math.cos(args.get(0)));
            case "tg":
                if (args.size() != 1) {
                    throw new ParsingException("Wrong number of arguments in tg()");
                }
                return new Pair<>(true, Math.tan(args.get(0)));
            case "sqrt":
                if (args.size() != 1) {
                    throw new ParsingException("Wrong number of arguments in sqrt()");
                }
                return new Pair<>(true, Math.sqrt(args.get(0)));
            case "pow":
                if (args.size() != 2) {
                    throw new ParsingException("Wrong number of arguments in pow()");
                }
                return new Pair<>(true, Math.pow(args.get(0), args.get(1)));
            case "abs":
                if (args.size() != 1) {
                    throw new ParsingException("Wrong number of arguments in abs()");
                }
                return new Pair<>(true, Math.abs(args.get(0)));
            case "sign":
                if (args.size() != 1) {
                    throw new ParsingException("Wrong number of arguments in sign()");
                }
                return new Pair<>(true, Math.signum(args.get(0)));
            case "log":
                if (args.size() != 2) {
                    throw new ParsingException("Wrong number of arguments in log()");
                }
                return new Pair<>(true, Math.log(args.get(0))/Math.log(args.get(1)));
            case "log2":
                if (args.size() != 1) {
                    throw new ParsingException("Wrong number of arguments in log2()");
                }
                return new Pair<>(true, Math.log(args.get(0))/Math.log(2.0));
            case "rnd":
                if (args.size() != 0) {
                    throw new ParsingException("Wrong number of arguments in rnd()");
                }
                return new Pair<>(true, Math.random());
            case "max":
                if (args.size() != 2) {
                    throw new ParsingException("Wrong number of arguments in max()");
                }
                return new Pair<>(true, Math.max(args.get(0), args.get(1)));
            case "min":
                if (args.size() != 2) {
                    throw new ParsingException("Wrong number of arguments in min()");
                }
                return new Pair<>(true, Math.min(args.get(0), args.get(1)));
            default:
                return new Pair<>(false, 1.0);
        }
    }

    private String getVariableOrFunction() throws ParsingException, IOException, Exception {
        getEps();
        StringBuilder temp = new StringBuilder("");
        if (Character.toString(strToCalculate.charAt(position)).matches("[a-zA-Z]")) {
            temp.append(strToCalculate.charAt(position));
            position++;
        }
        while (Character.toString(strToCalculate.charAt(position)).matches("[0-9a-zA-Z]")) {
            temp.append(strToCalculate.charAt(position));
            position++;
        }

        // No variable and function
        if (temp.toString().equals("")) {
            return temp.toString();
        }

        // Variable
        if (strToCalculate.charAt(position) != '(') {
            String variable = temp.toString();
            String answer = "";

            // First in params, second in users variables
            if (numberedParameters.containsKey(variable)) {
                answer += numberedParameters.get(variable);
            } else {
                if (variablesTable.containsKey(variable)) {
                    answer += variablesTable.get(variable).toString();
                } else {
                    throw new IOException("No such variable!");
                }
            }
            return answer;
        }

        position++;
        ArrayList<Double> argList = new ArrayList<>();

        while (strToCalculate.charAt(position) != ')') {
            getEps();
            //StringBuilder argument = new StringBuilder("");
            Double argument = 0.;
            while (strToCalculate.charAt(position) != ',' && strToCalculate.charAt(position) != ')') {
                argument = getSum();
            }

            argList.add(argument);

            if (strToCalculate.charAt(position) == ')') {
                position++;
                break;
            }

            position++;
        }
        String function = temp.toString();

        Pair<Boolean, Double> predef = Predefined(function, argList);
        if (predef.getKey()) {
            return predef.getValue().toString();
        }
        if (!functionTable.containsKey(function)) {
            throw new IOException("No such function!");
        }
        Evaluator func = functionTable.get(function);
        func.setArgs(argList);
        Double res = func.eval();
        return res.toString();
    }

    private Double getBrackets() throws ParsingException, Exception {
        String number = getPoint();
        getEps();
        if (number.length() != 0) {
            return Double.parseDouble(number);
        }

        number = getVariableOrFunction();
        if (number.length() != 0) {
            return Double.parseDouble(number);
        }

        if (strToCalculate.charAt(position) == '(') {
            position++;
            Double result = getSum();
            if (strToCalculate.charAt(position) == ')') {
                position++;
                return result;
            } else {
                throw new ParsingException("Close bracket expected");
            }
        } else if (strToCalculate.charAt(position) != '-') {
            throw new ParsingException("Unexpected symbol");
        } else {
            return -0.0;
        }
    }

    private Double getMul() throws ParsingException, Exception {
        Double number = getBrackets();
        getEps();
        while (strToCalculate.charAt(position) == '*' ||
                strToCalculate.charAt(position) == '/') {
            char operation = strToCalculate.charAt(position++);
            getEps();
            if (operation == '*') {
                number *= getBrackets();
            } else if (operation == '/') {
                number /= getBrackets();
            }
        }
        return number;
    }

    private Double getSum() throws ParsingException, Exception {
        getEps();
        Double number = getMul();
        while (strToCalculate.charAt(position) == '+' ||
                strToCalculate.charAt(position) == '-') {
            char operation = strToCalculate.charAt(position++);
            getEps();
            if (operation == '+') {
                number += getMul();
            } else if (operation == '-') {
                number -= getMul();
            }
        }
        return number;
    }

    private Double getResult() throws ParsingException, Exception {
        position = 0;
        Double result = getSum();

        if (position == strToCalculate.length() - 1) {
            return result;
        } else {
            throw new ParsingException("Unknown character");
        }
    }

    @Override
    public Double eval() throws ParsingException, Exception {

        if (strToCalculate == null) {
            throw new ParsingException("Null expression");
        }
        if (strToCalculate.charAt(strToCalculate.length() - 1) != '#') {
            strToCalculate = strToCalculate.concat("#");
        }
        return getResult();
    }

    @Override
    public void setArgs(ArrayList<Double> arguments) throws Exception {
        if (arguments.size() != fromNumberToPar.size()) {
            throw new Exception("The number of parameters does not match!");
        }

        for (int index = 0; index < arguments.size(); index++) {
            numberedParameters.put(fromNumberToPar.get(index), arguments.get(index));
        }
    }

    @Override
    public ArrayList<String> getParameters() {
        return new ArrayList<String>(fromNumberToPar.values());
    }

    @Override
    public String getBody() {
        return strToCalculate;
    }

    @Override
    public void setFuncTab(HashMap<String, Evaluator> funcTab) {
        this.functionTable = funcTab;
    }

    @Override
    public void setVarTab(HashMap<String, Double> varTab) {
        this.variablesTable = varTab;
    }
}