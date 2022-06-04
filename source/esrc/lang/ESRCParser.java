
package esrc.lang;

import java.util.ArrayList;
import java.util.List;

public interface ESRCParser {

  static String convertCode(String className, String code) {
    code = convertAsBlocks(code);
    code = convertBracketlessStatements(code);
    code = convertSyntax(code);
    code = convertTryCatchBlocks(code);
    code = convertOperations(code);
    code = convertKeywords(code);
    code = convertConstructor(className, code);
    code = convertModifiers(code);
    code = wrapScript(className, code);
    code = convertImports(code);
    code = appendSemicolons(code);
    code = correctElseBlocks(code);
    return code;
  }

  static String convertSpacingOfLine(String line, String head) {
    int spacing = analyseSpacingOfLine(head);
    String trimmed = new String();
    for(int i = analyseSpacingOfLine(line); i < line.length(); i++) trimmed += line.charAt(i);
    for(int i = 0; i < spacing; i++) trimmed = head.charAt(0) + trimmed;
    return trimmed;
  }

  String[] BRACKETLESS_STATEMENTS = new String[] { "if", "for", "while" };

  static String convertBracketlessStatements(String code) {
    String output = new String();
    for(String line : code.split("\n")) {
      for(String condition : BRACKETLESS_STATEMENTS) {
        if(line.contains(condition + ' ')) {
          line = replaceNonString(line, condition + ' ', condition + '(');
          line = replaceNonString(line, ':', "):");
          break;
        }
      }
      output += line + '\n';
    }
    return output;
  }

  static String convertAsBlocks(String code) {
    String newCode = new String();
    int asStatement = 0;
    int prevSpacing = 0;
    int asSpacing = 0;
    char spaceHeader = ' ';
    for(String line : code.split("\n")) {
      int postSpacing = analyseSpacingOfLine(line);
      if(line.contains(" as ")) {
        asStatement++;
        asSpacing = postSpacing;
        spaceHeader = line.charAt(0);
        line = replaceNonString(line, ':', "->");
      }
      line = replaceNonString(line, " as ", '(');
      if(asStatement > 0 && postSpacing < prevSpacing) {
        String newLine = new String();
        for(int i = 0; i < postSpacing; i++) newLine += spaceHeader;
        newCode += newLine + ")\n";
        asStatement--;
      }
      prevSpacing = postSpacing;
      newCode += line + '\n';
    }
    if(asStatement > 0) {
      for(int i = 0; i < asSpacing; i++) newCode += spaceHeader;
      newCode += ")\n";
    }
    return newCode;
  }

  static String correctElseBlocks(String code) {
    String[] newLines = code.split("\n");
    for(int i = 0; i < newLines.length; i++)
      if(newLines[i].contains("else"))
        if(newLines[i - 1].trim().equals("};"))
          newLines[i - 1] = newLines[i - 1].replace("};", "}");
    String newCode = new String();
    for(String line : newLines) newCode += line + '\n';
    return newCode;
  }

  static String convertTryCatchBlocks(String code) {
    String[] newLines = code.split("\n");
    for(int i = 0; i < newLines.length; i++)
      if(newLines[i].contains("error {")) {
        newLines[i - 1] = new String();
        newLines[i] = replaceNonString(newLines[i], "error {", "} error {");
      }
    String newCode = new String();
    for(String line : newLines) newCode += line + '\n';
    newCode = replaceNonString(newCode, "error {", "catch(Exception e) {");
    return newCode;
  }

  static String appendSemicolons(String code) {
    String colonCode = new String();
    for(String line : code.replace('\r', '\n').split(String.valueOf('\n')))
        colonCode += (
                line.endsWith("{")
                        || line.endsWith(",")
                        || line.trim().length() <= 0
                        ? line : line + ';'
        ) + '\n';
    return colonCode;
  }

  static int analyseSpacing(String code) {
    String[] lines = code.split("\n");
    for(String line : lines) {
      if(line.trim().isEmpty()) continue;
      else if(line.charAt(0) == '\t' || line.charAt(0) == ' ')
      return analyseSpacingOfLine(line);
    }
    return 0;
  }

  static int analyseSpacingOfLine(String line) {
    int spacing = 0;
    for(char c : line.toCharArray())
    if(c == '\t' || c == ' ') spacing++;
    else break;
    return spacing;
  }

  static String convertSyntax(String code) {
    code = replaceNonString(code, "->", "-> {");
    code = replaceNonString(code, ":", " {");
    code = replaceNonString(code, " { {", "::");
    String newCode = new String();
    int spacing = analyseSpacing(code);
    int concurrent = 0;
    for(String line : code.split("\n")) {
      if(line.trim().isEmpty()) continue;
      int lineSpacing = analyseSpacingOfLine(line);
      if(lineSpacing < concurrent) {
        int bracketsNeeded = (concurrent - lineSpacing) / spacing;
        for(int i = 0; i < bracketsNeeded; i++) {
          if(line.trim().equals(")")) line = "}" + line;
          else line = "}\n" + line;
        }
      }
      concurrent = lineSpacing;
      newCode += line + '\n';
    }
    while(concurrent > 0) {
      newCode += "\n}";
      concurrent -= spacing;
    }
    return newCode;
  }

  static String wrapScript(String className, String code) {
    return "@SuppressWarnings(\"unchecked\") public class " + className + " {\n" + code + "\n}";
  }

  static String convertModifiers(String code) {
    code = replaceNonString(code, '$', "final ");
    code = replaceNonString(code, '#', "static ");
    code = replaceNonString(code, "local ", "private ");
    code = replaceNonString(code, "secured ", "protected ");
    code = replaceNonString(code, "global ", "public ");
    return code;
  }

  static String convertImports(String code) {
    String cut = new String();
    String imports = "import esrc.lang.*;\n";
    boolean usingDirectives = false;
    for(String line : code.split(String.valueOf('\n'))) {
      if(line.length() > 1 && line.startsWith(String.valueOf(';'))) line = line.substring(1);
      if(usingDirectives) {
        if(line.trim().equalsIgnoreCase(String.valueOf('}'))) usingDirectives = false;
        else {
          String prefix = line.trim().split("/")[0];
          String sub = line.trim().substring(prefix.length());
          switch(prefix) {

            case "JAR":
              break;

            default:
              imports += "import " + line.trim() + ".*;\n";

          }

        }
      } else if(line.trim().equalsIgnoreCase("using {")) usingDirectives = true;
      else cut += line + '\n';
    }
    return imports + cut;
  }

  static String[] getJarImports(String code) {
    code = convertSyntax(code);
    boolean usingDirectives = false;
    String jars = new String();
    for(String line : code.split(String.valueOf('\n'))) {
      if(line.length() > 1 && line.startsWith(String.valueOf(';'))) line = line.substring(1);
      if(usingDirectives) {
        if(line.trim().equalsIgnoreCase(String.valueOf('}'))) usingDirectives = false;
        else {
          String prefix = line.trim().split("/")[0];
          if(prefix.equals("JAR")) jars += line.trim().substring(prefix.length() + 1) + ", ";
        }
      } else if(line.trim().equalsIgnoreCase("using {")) usingDirectives = true;
    }
    return jars.split(", ");
  }

  static String convertConstructor(String className, String code) {
    return replaceNonString(code, "entry {", "public static " + className + " SCRIPT; public " + className + "() { SCRIPT = this; ");
  }

  static String replaceNonString(String code, Object key, Object value) {
    List<String> parts = new ArrayList<String>();
    String current = new String();
    boolean inString = false;
    char previous = ' ';
    for(char ch : code.toCharArray()) {
        if(previous != '\\' && (ch == '\"' || ch == '\'')) {
            if(inString) current += ch;
            parts.add(current);
            current = new String();
            if(!inString) current += ch;
            inString = !inString;
        } else current += ch;
        previous = ch;
    }
    parts.add(current);
    String sKey = key.toString();
    String sValue = value.toString();
    String result = new String();
    for(String part : parts) {
        if(!part.startsWith(String.valueOf('\"')) && !part.startsWith(String.valueOf('\'')))
            part = part.replace(sKey, sValue);
        result += part;
    }
    return result;
  }

  static String convertKeywords(String code) {
    code = replaceNonString(code, "string", "String");
    code = replaceNonString(code, "object", "Object");
    code = replaceNonString(code, "String.empty", "\"\"");
    code = replaceNonString(code, "int.parse(", "Integer.parseInt(");
    code = replaceNonString(code, "float.parse(", "Float.parseFloat(");
    code = replaceNonString(code, "double.parse(", "Double.parseDouble(");
    code = replaceNonString(code, "long.parse(", "Long.parseLong(");
    code = replaceNonString(code, "short.parse(", "Short.parseShort(");
    code = replaceNonString(code, "function ", "void ");
    code = replaceNonString(code, "class ", "static class ");
    code = replaceNonString(code, "loop {", "while(true) { if(false) break; ");
    code = replaceNonString(code, "subString", "substring");
    code = replaceNonString(code, "StringWidth", "stringWidth");
    code = replaceNonString(code, "str(", "String.valueOf(");
    code = replaceNonString(code, "<handle>", "()");
    return code;
  }

  static String convertOperations(String code) {
    code = replaceNonString(code, " and ", " && ");
    code = replaceNonString(code, " or ", " || ");
    code = replaceNonString(code, " in ", " : ");
    code = replaceNonString(code, " not ", " ! ");
    code = replaceNonString(code, " is ", " == ");
    code = replaceNonString(code, " assign ", " = ");
    code = replaceNonString(code, " ++ ", " ? ");
    code = replaceNonString(code, " -- ", " : ");
    return code;
  }

}
