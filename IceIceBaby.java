import java.util.*;

/**
 * A class for the I Before E Etude of COSC326
 * Checks if a given string is valid in a given alphabet with certain spelling rules
 * or given an integer, outputs the total number of valid strings of that length.
 *
 * @author Joshua Whitney, James Strathern, Ryan Collins, Aaron Anderson
 */
public class IceIceBaby{

  /**
   * A collection of static global variables, for use across the various functions
   * including the recursive loop.
   *
   * alphabet - a simple regex the input alphabet formatted as [abc]+
   * forbiddens - an array of all the input forbidden strings, in plaintext, without exceptions.
   * exceptions - an two-dimensional array, with the first dimension corresponding to each of 
   *  the members within forbiddens, the second dimension containing each respective exception.
   * trivials - a map, with keys being a given letter that the string to be checked against
   *  and the value being an array of regex expressions relevant to strings ending in that
   * key. Space is used for the key of trivials to always be checked regardless of letter.
   * memo - a map used for memoisation over the course of the recursive loop, the key is a string
   *  formatted as a regex expression found in trivials, followed by an integer representing the
   *  distance from the leaf node it is being memoised at, eg ".*[abc]$1", and the value being a
   *  long representing the number of valid strings resulting in the tree rooted at the key.
   * print - a simple toggle boolean to turn on println statements if the -p argument is used, 
   *  causing them to output debugging printlns. Incompatible with tree mode.
   * slow - a simple toggle boolean to turn off memoisation and other optimisations when
   *  using the -s command, to run a method that while slower, is guaranteed to be correct.
   * tree - a simple toggle boolean to turn on tree mode, causing a more visual display of the
   *  nature of the constructed strings and their relationships.
   */
  static String alphabet;
  static ArrayList<String> forbiddens = new ArrayList<String>();
  static ArrayList<ArrayList<String>> exceptions = new ArrayList<ArrayList<String>>();
  static HashMap<String, ArrayList<String>> trivials = new HashMap<String, ArrayList<String>>();
  static HashMap<String, Long> memo = new HashMap<String, Long>();
  static boolean print = false;
  static boolean slow = false;
  static boolean tree = false;
  
  /**
   * The main method, through which the rest of the program interfaces.
   * 
   * @param args The command line arguments, -p causes output for debugging to be printed, -s causes slow mode
   */
  public static void main(String[] args){
    Scanner scan = new Scanner(System.in);
    String input;
    
    try{
      if(args.length == 1){
        if(args[0].equals("-p")) print = true; //Toggles the print variable if -p is used.
        else if(args[0].equals("-s")) slow = true; //Toggles the slow variable if -s is used.
        else if(args[0].equals("-t")) tree = true; //Toggles the tree variable if -t is used.
        else throw new Exception();
      }
      if(args.length == 2){
        if(args[0].equals("-s") || args[1].equals("-s")){
          slow = true; //Toggles the slow variable if -s is used.
          if(args[0].equals("-p") || args[1].equals("-p")) print = true; //Toggles the print variable if -p is used.
          else if(args[0].equals("-t") || args[1].equals("-t")) tree = true; //Toggles the slow variable if -s is used.
          else throw new Exception();
      	}
      	else throw new Exception();
      }
 	}catch(Exception e){
 	  System.out.println("Please use the form \"java IceIceBaby [arguments]\"");
 	  System.out.println("List of Valid Arguments:");
 	  System.out.println("  -p  Shows detailed working during length-based generation. Incompatible with -t.");
 	  System.out.println("  -t  Shows a more visual tree-like structure during length-based generation. Incompatible with -p.");
 	  System.out.println("  -s  Enables \"Slow Mode\", no optimisations during length-based generation.");
 	  return;
 	}
    
    alphabet = "[" + scan.nextLine() + "]+"; //Scans the alphabet from the first line in System.in
    int i = 0;
    try{ //Continuously Scans the next lines as the rules/exceptions input, until a blank line.
      while(scan.hasNextLine()){
        input = scan.nextLine();
        Scanner sc = new Scanner(input);
        sc.useDelimiter(" ");
        String to_forbid = sc.next();
        forbiddens.add(to_forbid);
        exceptions.add(new ArrayList());
        while(sc.hasNext()){
          exceptions.get(i).add(sc.next());
        }
        i++;
      }
    }catch(NoSuchElementException nseex){} //Catches the empty line to carry on with validation input.
    
    populateTrivials();
    while(scan.hasNextLine()){
      input = scan.nextLine();
      try{ //Generates number of valid strings for a given integer input, or validated a single string.
        int l = Integer.parseInt(input);
        memo = new HashMap();
        System.out.println(generateValids("", l, 0));
      }catch(NumberFormatException nfex){ //Catches for the non-integer string validation.
        System.out.println(isValid(input));
      }
    }
  }
  
  /**
   * The single string validation method
   * Simply reads the string from left to right, checking for exceptions & forbiddens
   * along the way.
   *
   * @param s The string to be validated.
   * @return either "Valid" or "Invalid".
   */
  public static String isValid(String s){
    if(!s.matches(alphabet)){
      return "Invalid";
    }
    boolean isValid = true;
    for(int i = 0; i < s.length() && isValid; i++){ //Cumulatively checks each additional letter
      isValid = checkBack(s.substring(0, i+1));
    }
    if(isValid){
      return "Valid";
    }else{
      return "Invalid";
    }
  }
  
  /**
   * A simple method to check the end of a string to all the relevant forbiddens/exceptions,
   * as is assumes the program has previously checked the string up until that point as well.
   *
   * @param check The string to validate.
   * @return a boolean, true for Valid, false for Invalid.
   */
  public static boolean checkBack(String check){
    for(int i = 0; i < forbiddens.size(); i++){ //Formulates a regex for each forbidden an checks for it.
      String forbid = ".*" + forbiddens.get(i) + "$";
      boolean exceptional = false;
      if(check.matches(forbid)) {
        for(int j = 0; j < exceptions.get(i).size() && !exceptional; j++){ //If forbidden, checks for preceeding exceptions.
          String exception = ".*" + exceptions.get(i).get(j) + forbiddens.get(i) + "$";
          if(check.matches(exception)) {
            exceptional = true;
          }
        }
        if(!exceptional) {
          return false;
        }
      }
    }
    return true;
  }
  
  /**
   * A complex method that generates the various maps of regex which the other methods use
   * to validate strings, based on the input alphabet and rules.
   *
   * This generates our trivials sets, defined here:
   * 
   *
   * single-trivial - Letters for which the value of their subtree does not change based on the 
   *  character before them in the string. irrelevant trivial is a subset of single-trivials,
   * that all share the same value. The remaining subset are letters that only occur as the 
   * first letter of one or more exceptions.
   * irrelevant-trivial - Letters not present in any of the rules, all effectively equal to each other.
   *  A tree with one of these as the root has the same value as all the other irrelevenat-trivial
   *  trees, at a given level. irrelevant-trivial is a subset of single-trivial.
   * double-trivial - The rule set of double-trivials is based on actually checking the last two letters
   *  of the given string, as some letters become subjectively trivial when the letter preceeding them
   *  does not preceed that letter in any of the rules, for example given the rule "ab c", the letter b
   *  only has a resulting tree below it if preceeded by an a, so the double-trivials set would include
   *  a rule saying .*[^a][b]$
   * triple-trivial - Similar to double-trivials, but a much smaller and more niche set of rules that 
   *  only apply to letters that have rules in which there can be up to two relevant letters preceeding
   *  that change the value of the resulting subtree.
   * exceptional-trivial - Simply a trival case for each forbidden combined with each of its respective
   *  exceptions, as a final catch assuming none of the letters are involved in other cases.
   */
  public static void populateTrivials(){
    for(int i = 1; i < alphabet.length() - 2; i++){ //Initialises the value arrays for each letter.
      trivials.put("" + alphabet.charAt(i), new ArrayList<String>());
    }
    trivials.put(" ", new ArrayList<String>()); //Adds a value array for " ", the rules that will always be checked.
    
    String singles = ".*[";
    for(int i = 1; i < alphabet.length() - 2; i++){ //Loops through the alphabet, which position in the rules each letter occurs in.
      String x = "" + alphabet.charAt(i);
      boolean isTrivial = true;
      boolean isFirstOfException = false;
      for(int j = 0; j < forbiddens.size(); j++){
        if(!forbiddens.get(j).contains(x)) {
          for(int k = 0; k < exceptions.get(j).size(); k++){
            for(int l = 0; l < exceptions.get(j).get(k).length(); l++){
              if(("" + exceptions.get(j).get(k).charAt(l)).equals(x)){
                if(l == 0 && isTrivial){
                  isFirstOfException = true;
                }else{
                  isTrivial = false;
                  isFirstOfException = false;
                }
              }
            }
          }
        }else{
          isTrivial = false;
          isFirstOfException = false;
        }
      }
      if(isFirstOfException){ //If the letter is only ever found at the beginning of an exception, it is single-trivial.
        trivials.get("" + x).add(".*[" + x + "]$");
      }else{
        if(isTrivial){ //If the letter is not found in any rules at all, it is irrelevant-trivial.
          singles += x;
        }
      }
    }
    
    String bucket = "^[^";
    for(int i = 0; i < forbiddens.size(); i++) { //Every leter that is NOT the start of a forbidden is single-trivial at the start of a word.
      bucket += forbiddens.get(i).charAt(0);
      for(int j = 0; j < exceptions.get(i).size(); j++){ //Every leter that is NOT the start of an exception is single-trivial at the start of a word.
        bucket += exceptions.get(i).get(j).charAt(0);
        trivials.get("" + forbiddens.get(i).charAt(forbiddens.get(i).length() - 1)).add( ".*" + exceptions.get(i).get(j) + forbiddens.get(i) + "$");
      } //Each combination of forbiddens and each of their respective exceptions are exceptional-trivial.
    }
    bucket += "]";
    if(bucket.charAt(bucket.length() - 2) != '^') trivials.get(" ").add(bucket);
    
    HashMap<String, ArrayList<String>> relevant_pairs = new HashMap<String, ArrayList<String>>();
    HashMap<String, ArrayList<String>> relevant_triples = new HashMap<String, ArrayList<String>>();
    for(int i = 1; i < alphabet.length() - 2; i++){ //Generate a relevant pair structure for each single letter in the alphabet.
      relevant_pairs.put("" + alphabet.charAt(i), new ArrayList<String>());
      for(int j = 1; j < alphabet.length() - 2; j++){ //Generate a relevant triple structure for each letter pair in the alphabet.
        relevant_triples.put("" + alphabet.charAt(j) + alphabet.charAt(i), new ArrayList<String>());
      }
    }
    
    for(int i = 0; i < forbiddens.size(); i++){
      for(int k = 0; k < forbiddens.get(i).length(); k++){
        if(k > 0){ //For each of the single letter structures within the forbiddens, add to it a double-trivial case for each preceeding leter in the forbiddens.
          ArrayList<String> temp = relevant_pairs.get("" + forbiddens.get(i).charAt(k));
          temp.add("" + forbiddens.get(i).charAt(k-1));
          relevant_pairs.put("" + forbiddens.get(i).charAt(k), temp);
          if(k > 1){ //For each of the double letter structures within the forbiddens, add to it a triple-trivial case for each preceeding letter in the forbiddens. 
            relevant_triples.get("" + forbiddens.get(i).charAt(k-1) + forbiddens.get(i).charAt(k)).add("" + forbiddens.get(i).charAt(k-2));
          }else{
            for(int j = 0; j < exceptions.get(i).size(); j++){
              relevant_triples.get("" + forbiddens.get(i).charAt(k-1) + forbiddens.get(i).charAt(k) ).add("" + exceptions.get(i).get(j).charAt(exceptions.get(i).get(j).length() - 1));
            }
          } 
        }else{
          for(int j = 0; j < exceptions.get(i).size(); j++){ //For each of the single letter sructures within the exceptions, add to it a double-trivial for the preceeding exception letter, if applicable.
            relevant_pairs.get("" + forbiddens.get(i).charAt(k)).add("" + exceptions.get(i).get(j).charAt(exceptions.get(i).get(j).length() - 1));
            if(exceptions.get(i).get(j).length() > 1){ //For each of the double letter sructures within the exceptions, add to it a triple-trivial for the preceeding exception letter, if applicable.
              relevant_triples.get("" + exceptions.get(i).get(j).charAt(exceptions.get(i).get(j).length() - 1) + forbiddens.get(i).charAt(k)).add("" + exceptions.get(i).get(j).charAt(exceptions.get(i).get(j).length() - 2));
            }
          }
        }
      }
    }
    
    for(int i = 0; i < forbiddens.size(); i++){
      for(int j = 0; j < exceptions.get(i).size(); j++){
        for(int k = 0; k < exceptions.get(i).get(j).length(); k++){
          if(k > 0){ //For each of the single-letter structures within the exceptions, add to it a double-trivial case for the preceeding letter in exceptions.
            relevant_pairs.get("" + exceptions.get(i).get(j).charAt(k)).add("" + exceptions.get(i).get(j).charAt(k - 1));
          }
          if(k > 1){ //For each of the double-letter structures within the exceptions, add to it a triple-trivial case for the preceeding letter in exceptions.
            relevant_triples.get("" + exceptions.get(i).get(j).charAt(k) + exceptions.get(i).get(j).charAt(k-1)).add("" + exceptions.get(i).get(j).charAt(k - 2));
          }
        }
      }
    }
    
    for(int i = 1; i < alphabet.length() - 2; i++){
      String pair_test = "";
      if(!relevant_pairs.get("" + alphabet.charAt(i)).isEmpty()){
        pair_test += ".*[^";
        for(String c : relevant_pairs.get("" + alphabet.charAt(i))){ //Create double-trivial case containing each letter NOT relevant to each single letter structure.
          pair_test += c;
        }
        pair_test += "][" + alphabet.charAt(i) + "]$";
        trivials.get("" + alphabet.charAt(i)).add(pair_test);
      }
    }
  
    for(String key : relevant_triples.keySet()){
      String triple_test = "";
      if(!relevant_triples.get(key).isEmpty()){
        triple_test += ".*[^";
        for(String c : relevant_triples.get(key)){ //Create triple-trivial case containing each letter NOT relevant to each double letter structure.
              triple_test += c;
        }
        triple_test += "][" + key.charAt(0) + "][" + key.charAt(1) + "]$";
        trivials.get("" + key.charAt(1)).add(triple_test);
      }
    }
    
    if(singles.charAt(singles.length()-1) != '[') {
      singles += "]$";
      trivials.get(" ").add(singles); //Add each of the single-trivial cases last, since they should be caught last of all cases.
    }

     if(print) System.out.println(trivials); //Print all the accumulated trivial cases to System.out if -p is used.
     
  }
  
  /**
   * A recursive method that is the main computational backbone of the program, which generates
   * every possible permutation of letters for the given length, while taking into account certain
   * optimizations, such as pruning upon encountering an invalid substring, and memoising to each 
   * of the trivial cases at each level.
   * 
   * @param check The current accumulated substring to be constructed and checked.
   * @param stop The total desired length of the string being constructed.
   * @param level The current length of the string being constructed.
   * @return The cumulative total of all valid strings resulting from the current substring.
   */
  public static long generateValids(String check, int stop, int level){
    String capture = null;
    
    if(level == stop) { //If the substring has reached the desired length, and has not already been pruned, it is valid string.
      if(print) System.out.println(check + " Valid 1"); //Outputs whenever a valid string has been found to System.out if -p is used.
      if(tree) { //Output in tree form if -t is used
       	System.out.println(check + "|");
      }
      return 1;
    }
    
    if(check.length() > 0 && !trivials.isEmpty() && !slow){ //Only activates memoisation if not in slow mode
      String last_char = "" + check.charAt(check.length() - 1);
      for(int i = 0; i < trivials.get(" ").size() && level != 0 && capture == null; i++){ //Check all of the mandatory trivial cases that apply to all strings for memoisation.
        if(check.matches(trivials.get(" ").get(i))){
          if(memo.containsKey(trivials.get(" ").get(i)+(stop-level))) { //If the current substring matches a trivial case, return the value if available, or flag for capture.
            if(print) System.out.println(check + " retrieved from " + (trivials.get(" ").get(i)+(stop-level)) + " for value " + memo.get(trivials.get(" ").get(i)+(stop-level)));
            if(tree) { //Output in tree form if -t is used
           	  System.out.println(check + "|*");
           	}
            return memo.get(trivials.get(" ").get(i)+(stop-level));
          }
          capture = trivials.get(" ").get(i)+(stop-level);
        }
      }
      for(int i = 0; i < trivials.get(last_char).size() && level != 0 && capture == null; i++){ //Check all of the subjectively trivial cases that apply to strings ending in the currrent last letter.
        if(check.matches(trivials.get(last_char).get(i))){
          if(memo.containsKey(trivials.get(last_char).get(i)+(stop-level))) { //If the current substring matches a trivial case, return the value if available, or flag for capture.
            if(print) System.out.println(check + " retrieved from " + (trivials.get(last_char).get(i)+(stop-level)) + " for value " + memo.get(trivials.get(last_char).get(i)+(stop-level)));
            if(tree) { //Output in tree form if -t is used
           	  System.out.println(check + "|*");
           	}
            return memo.get(trivials.get(last_char).get(i)+(stop-level));
          }
          capture = trivials.get(last_char).get(i)+(stop-level);
        }
      }
    }
    
    long valids = 0;
    for(int i = 1; i < alphabet.length()-2; i++){ //For each letter in the alphabet, generate the number of valid strings results from the current substring + that letter.
      String test = check + alphabet.charAt(i);
      if(checkBack(test)) valids += generateValids(test, stop, level+1);
      else if(print) System.out.println(test + " pruned"); //Output to System.out if the letter would cause an invalid substring, if -p is used.
      else if(tree) { //Output in tree form if -t is used
		System.out.println(check + alphabet.charAt(i) + "|#");
   	  }
    }
    if(capture != null) { //If the current substring was flagged for capture, once the total is calculated, store it in the memoisation structure under the appropriate trivial case.
      memo.put(capture, valids);
      if(print) System.out.println(check + " stored at " + capture + " for value " + memo.get(capture)); //Output when a value is stored to System.out if -p is used.
    }
    if(tree && check.length()-1 != -1) { //Output in tree form if -t is used
   	  System.out.println(check + "/");
   	}
    return valids; //Return the current cumulative total based on the current substring.
    
  }
}
