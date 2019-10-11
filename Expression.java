package app;

import java.io.*;

import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

	public static String delims = " \t*+-/()[]";

	/**
	 * Populates the vars list with simple variables, and arrays lists with arrays
	 * in the expression. For every variable (simple or array), a SINGLE instance is created 
	 * and stored, even if it appears more than once in the expression.
	 * At this time, values for all variables and all array items are set to
	 * zero - they will be loaded from a file in the loadVariableValues method.
	 * 
	 * @param expr The expression
	 * @param vars The variables array list - already created by the caller
	 * @param arrays The arrays array list - already created by the caller
	 */
	public static void 
	makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
		StringTokenizer temp = new StringTokenizer(expr, delims, true);
		while(temp.hasMoreElements()) {
			String word = temp.nextElement().toString();
			if(Character.isDigit(word.charAt(0))) {
				continue;
			} else {
				if(delims.contains(word)){  
					continue;
				} else if(!temp.hasMoreElements()) {
					vars.add(new Variable(word));
				} else if(temp.nextElement().equals("[")){
					arrays.add(new Array(word));
				} else {
					vars.add(new Variable(word));
				}		
			}
		}

		/** COMPLETE THIS METHOD **/
		/** DO NOT create new vars and arrays - they are already created before being sent in
		 ** to this method - you just need to fill them in.
		 **/
	}

	/**
	 * Loads values for variables and arrays in the expression
	 * 
	 * @param sc Scanner for values input
	 * @throws IOException If there is a problem with the input 
	 * @param vars The variables array list, previously populated by makeVariableLists
	 * @param arrays The arrays array list - previously populated by makeVariableLists
	 */
	public static void 
	loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays) 
			throws IOException {
		while (sc.hasNextLine()) {
			StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
			int numTokens = st.countTokens();
			String tok = st.nextToken();
			Variable var = new Variable(tok);
			Array arr = new Array(tok);
			int vari = vars.indexOf(var);
			int arri = arrays.indexOf(arr);
			if (vari == -1 && arri == -1) {
				continue;
			}
			int num = Integer.parseInt(st.nextToken());
			if (numTokens == 2) { // scalar symbol
				vars.get(vari).value = num;
			} else { // array symbol
				arr = arrays.get(arri);
				arr.values = new int[num];
				// following are (index,val) pairs
				while (st.hasMoreTokens()) {
					tok = st.nextToken();
					StringTokenizer stt = new StringTokenizer(tok," (,)");
					int index = Integer.parseInt(stt.nextToken());
					int val = Integer.parseInt(stt.nextToken());
					arr.values[index] = val;              
				}
			}
		}
	}

	/**
	 * Evaluates the expression.
	 * 
	 * @param vars The variables array list, with values for all variables in the expression
	 * @param arrays The arrays array list, with values for all array items
	 * @return Result of evaluation
	 */
	private static boolean isNumeric(String str) { 
		try {  
			Double.parseDouble(str);  
			return true;
		} catch(NumberFormatException e){  
			return false;  
		}  
	}
	
	public static float 
	evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {

		Stack<String> nums = new Stack<String>();
		Stack<String> operator = new Stack<String>();

		for(int i = 0; i < vars.size(); i++) {
			expr = expr.replaceAll("\\b"+vars.get(i).name+"\\b",""+vars.get(i).value);
		}
		expr = expr.replaceAll("\\s+","");

		String tempExpr = "(" + expr + ")" + ")";
		StringTokenizer temp = new StringTokenizer(tempExpr, delims, true);
		String currentChar = temp.nextToken();
		boolean isNumber = isNumeric(currentChar);
		if(temp.countTokens() == 1) {
			float wordd = Float.parseFloat(currentChar);
			return wordd;
		}

		while(temp.hasMoreTokens()) {

			if(isNumber) { 
				nums.push(currentChar);
			}else if(currentChar.equals("(") || currentChar.equals("[") || currentChar.equals("-") || currentChar.equals("+")) {
				operator.push(currentChar);
			} else if(Character.isLetter(currentChar.charAt(0))) {
				operator.push(currentChar);
			}else if(currentChar.equals("*") || currentChar.equals("/")) {
				String nextCh = "";
				nextCh = temp.nextToken();
				if(nextCh.equals("(")) {
					operator.push(currentChar);
					currentChar = nextCh;
					continue;
				}

				if(currentChar.equals("*")) {
					float num2 = Float.parseFloat(nums.pop());
					float num1 = Float.parseFloat(nextCh);
					float endGame = num1*num2;
					String convert = Float.toString(endGame);
					nums.push(convert);
				} else if(currentChar.equals("/")) {
					float num2 = Float.parseFloat(nums.pop());
					float num1 = Float.parseFloat(nextCh);
					float endGame = num2/num1;
					String convert = Float.toString(endGame);
					nums.push(convert);
				}
				currentChar = nextCh;
				continue;

			} else if (currentChar.equals(")")) {
				Stack<String> newNum = new Stack<String>();
				Stack<String> newOperator = new Stack<String>();
				while(!operator.peek().equals("(")) {
					newNum.push(nums.pop());		// make it read left to right instead of opp.
					newOperator.push(operator.pop());
				}
				newNum.push(nums.pop());

				while(!newOperator.isEmpty() && (newOperator.peek().equals("+") || newOperator.peek().equals("-"))) {
					if(newOperator.peek().equals("+")) {
						float num2 = Float.parseFloat(newNum.pop());
						float num1 = Float.parseFloat(newNum.pop());
						float endGame = num1+num2;
						String convert = Float.toString(endGame);
						newNum.push(convert);
						newOperator.pop();
					} else if(newOperator.peek().equals("-")) {
						float num2 = Float.parseFloat(newNum.pop());
						float num1 = Float.parseFloat(newNum.pop());
						float endGame = num2-num1;
						String convert = Float.toString(endGame);
						newNum.push(convert);
						newOperator.pop();
					}
				}
				operator.pop();
				nums.push(newNum.pop());
				if(!operator.isEmpty()) {
					if(operator.peek().equals("*") || operator.peek().equals("/")) {
						if(operator.peek().equals("*")) {
							operator.pop();
							float num2 = Float.parseFloat(nums.pop());
							float num1 = Float.parseFloat(nums.pop());
							float endGame = num1*num2;
							String convert = Float.toString(endGame);
							nums.push(convert);
						} else if(operator.peek().equals("/")) {
							operator.pop();
							float num2 = Float.parseFloat(nums.pop());
							float num1 = Float.parseFloat(nums.pop());
							float endGame = num1/num2;
							String convert = Float.toString(endGame);
							nums.push(convert);
						}
					}
				}
			} else if(currentChar.equals("]")) {
				Stack<String> newNum = new Stack<String>();
				Stack<String> newOperator = new Stack<String>();
				while(!operator.peek().equals("[")) {
					newNum.push(nums.pop());		// make it read left to right instead of opp.
					newOperator.push(operator.pop());
				}
				newNum.push(nums.pop());

				while(!newOperator.isEmpty() && (newOperator.peek().equals("+") || newOperator.peek().equals("-"))) {
					if(newOperator.peek().equals("+")) {
						float num2 = Float.parseFloat(newNum.pop());
						float num1 = Float.parseFloat(newNum.pop());
						float endGame = num1+num2;
						String convert = Float.toString(endGame);
						newNum.push(convert);
						newOperator.pop();
					} else if(newOperator.peek().equals("-")) {
						float num2 = Float.parseFloat(newNum.pop());
						float num1 = Float.parseFloat(newNum.pop());
						float endGame = num2-num1;
						String convert = Float.toString(endGame);
						newNum.push(convert);
						newOperator.pop();
					}
				}
				operator.pop();
				nums.push(newNum.pop());
				if(nums.size() > 0) {
					String index = nums.pop();
					Array whoops = arrays.get(arrays.indexOf(new Array(operator.pop())));
					double num = Double.parseDouble(index);
					int numOfficial = (int)num;
					float result = whoops.values[numOfficial];
					String convert = Float.toString(result);
					nums.push(convert);
				}
			}
			currentChar = temp.nextToken();
			isNumber = isNumeric(currentChar);
		}
		float result = Float.parseFloat(nums.pop()); 
		return result;
	}
}