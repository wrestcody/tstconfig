package com.softwareloop.tstconfig;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrMatcher;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by predo on 27/08/14.
 */
public class Test {

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    enum DuplicatesPolicy {
        REPLACE,
        IGNORE,
        APPEND
    }

    public final static String INI_SECTION_HEADER_REGEX =
            "^\\s*\\[(.*)\\]\\s*$";
    public final static Pattern INI_SECTION_HEADER_PATTERN =
            Pattern.compile(INI_SECTION_HEADER_REGEX);

    public final static String APACHE_SECTION_HEADER_REGEXP =
            "^\\s*<(.*)>\\s*$";
    public final static String APACHE_SECTION_FOOTER_REGEXP =
            "^\\s*</.*>\\s*$";

    public final static Pattern APACHE_SECTION_HEADER_PATTERN =
            Pattern.compile(APACHE_SECTION_HEADER_REGEXP);
    public final static Pattern APACHE_SECTION_FOOTER_PATTERN =
            Pattern.compile(APACHE_SECTION_FOOTER_REGEXP);

    public final static String APT_SECTION_HEADER_REGEX =
            "^\\s*(\\S+)\\s*\\{\\s*$";
    public final static String APT_SECTION_FOOTER_REGEX =
            "^\\s*\\}\\s*;\\s*$";

    public final static Pattern APT_SECTION_HEADER_PATTERN =
            Pattern.compile(APT_SECTION_HEADER_REGEX);
    public final static Pattern APT_SECTION_FOOTER_PATTERN =
            Pattern.compile(APT_SECTION_FOOTER_REGEX);

    public final static StrMatcher COLON_SEPARATOR =
            StrMatcher.charMatcher(':');

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    List<String> lines;
    boolean linesParsed;
    int[] columnMap;
    DuplicatesPolicy duplicatesPolicy;

    Config config;
    List<String[]> section;
    String[] values;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Interface/abstract class implementations
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // File methods
    //--------------------------------------------------------------------------

    public void file(String... args) {
        resetConfig();
        String filename = StringUtils.join(args, ' ');
        try {
            lines = ConfigUtils.readLinesFromFile(filename);
        } catch (IOException e) {
            System.out.println("ERROR: cannot read file.");
        }
    }

    public void command(String... args) {
        resetConfig();
        lines = ConfigUtils.readLinesFromCommand(args);
    }

    public void skip_header_lines(String... args) {
        String nLinesStr = StringUtils.join(args, ' ');
        try {
            int nLines = Integer.parseInt(nLinesStr);
            config.setSkipHeaderLines(nLines);
        } catch (NumberFormatException e) {
            System.out.println("ERROR: not a number");
        }
    }

    public void columns(String... args) {
        columnMap = new int[args.length];
        for (int i = 0; i < args.length; i++) {
            try {
                int current = Integer.parseInt(args[i]);
                columnMap[i] = current;
            } catch (NumberFormatException e) {
                System.out.println("ERROR: not a number: " + args[i]);
            }
        }
    }

    void resetConfig() {
        lines = Collections.EMPTY_LIST;
        config = new Config();
        linesParsed = false;
        columnMap = null;
        section = Collections.EMPTY_LIST;
        values = null;
        duplicatesPolicy = DuplicatesPolicy.APPEND;
    }

    public void syntax(String... args) {
        String syntax = StringUtils.join(args, ' ');
        if ("apache".equals(syntax)) {
            config.setParseMode(Config.ParseMode.TOKENIZED);
            config.setSectionHeaderPattern(APACHE_SECTION_HEADER_PATTERN);
            config.setSectionFooterPattern(APACHE_SECTION_FOOTER_PATTERN);
            config.setHashCommentAllowed(true);
        } else if ("apt".equals(syntax)) {
            config.setParseMode(Config.ParseMode.TOKENIZED);
            config.setSectionHeaderPattern(APT_SECTION_HEADER_PATTERN);
            config.setSectionFooterPattern(APT_SECTION_FOOTER_PATTERN);
            config.setSeparator(StrMatcher.charSetMatcher(" \t;"));
            config.setSlashCommentAllowed(true);
        } else if ("etc_group".equals(syntax)
                || "etc_passwd".equals(syntax)
                || "etc_shadow".equals(syntax)) {
            config.setParseMode(Config.ParseMode.TOKENIZED);
            config.setSeparator(COLON_SEPARATOR);
            config.setHashCommentAllowed(true);
            config.setIgnoreEmptyTokens(false);
        } else if ("etc_hosts".equals(syntax)) {
            config.setParseMode(Config.ParseMode.TOKENIZED);
            config.setHashCommentAllowed(true);
        } else if ("fail2ban".equals(syntax)) {
            config.setParseMode(Config.ParseMode.KEYVALUE);
            config.setSectionHeaderPattern(INI_SECTION_HEADER_PATTERN);
            config.setKeySeparator("=");
            config.setHashCommentAllowed(true);
        } else if ("fixed".equals(syntax)) {
            config.setParseMode(Config.ParseMode.FIXED);
        } else if ("key_value".equals(syntax)) {
            config.setParseMode(Config.ParseMode.KEYVALUE);
        } else if ("ini".equals(syntax)) {
            config.setParseMode(Config.ParseMode.KEYVALUE);
            config.setSectionHeaderPattern(INI_SECTION_HEADER_PATTERN);
            config.setKeySeparator("=");
            config.setKeySeparatorOptional(true);
            config.setHashCommentAllowed(true);
            config.setSemicolonCommentAllowed(true);
        } else if ("ssh".equals(syntax)) {
            config.setParseMode(Config.ParseMode.TOKENIZED);
            config.setHashCommentAllowed(true);
        } else if ("swapon".equals(syntax)) {
            config.setParseMode(Config.ParseMode.TOKENIZED);
            config.setSkipHeaderLines(1);
        } else if ("tokenized".equals(syntax)) {
            config.setParseMode(Config.ParseMode.TOKENIZED);
        } else if ("ufw".equals(syntax)) {
            config = new UfwConfig();
        } else {
            System.out.println("ERROR: unrecognized syntax: " + syntax);
        }
    }

    public void key_separator(String... args) {
        String keySeparator = StringUtils.join(args, ' ');
        config.setKeySeparator(keySeparator);
    }

    public void separator(String... args) {
        String separator = StringUtils.join(args, ' ');
        config.setSeparator(StrMatcher.stringMatcher(separator));
    }

    public void ignore_empty_tokens(String... args) {
        String ignoreEmptyTokensStr = StringUtils.join(args, ' ');
        boolean ignoreEmptyTokens = Boolean.parseBoolean(ignoreEmptyTokensStr);
        config.setIgnoreEmptyTokens(ignoreEmptyTokens);
    }

    public void positions(String... args) {
        int[] positions = new int[args.length];
        for (int i = 0; i < args.length; i++) {
            positions[i] = Integer.parseInt(args[i]);
        }
        config.setPositions(positions);
    }

    void ensureConfigInitialized() {
        if (!linesParsed) {
            linesParsed = true;
            config.parseLines(lines);
            section = config.getTopLevelSection();
        }
    }

    //--------------------------------------------------------------------------
    // Section methods
    //--------------------------------------------------------------------------

    public void section(String... args) {
        ensureConfigInitialized();
        String sectionName = StringUtils.join(args, ' ');
        section = config.getSection(sectionName);
        if (section == null) {
            System.out.println("ERROR: section is undefined");
            section = Collections.EMPTY_LIST;
        }
    }

    public void section_size(String... args) {
        ensureConfigInitialized();
        values = new String[1];
        values[0] = Integer.toString(section.size());
    }

    //--------------------------------------------------------------------------
    // Property methods
    //--------------------------------------------------------------------------

    public void property(String... args) {
        ensureConfigInitialized();
        values = null;
        final String propertyName = StringUtils.join(args, ' ');
        boolean first = true;
        for (String[] line : section) {
            String firstColumn = getMappedColumn(line, 0);
            if (ObjectUtils.equals(propertyName, firstColumn)) {
                String[] lineValues = extractValues(line);
                if (first) {
                    first = false;
                    values = lineValues;
                } else {
                    switch (duplicatesPolicy) {
                        case REPLACE:
                            values = lineValues;
                        case APPEND:
                            values = ArrayUtils.addAll(values, lineValues);
                            break;
                        default:
                            /* IGNORE DUPLICATE */
                    }
                }
            }
        }
    }

    public String[] extractValues(String[] current) {
        if (columnMap == null) {
            return Arrays.copyOfRange(current, 1, current.length);
        } else {
            String[] result = new String[columnMap.length - 1];
            for (int i = 1; i < columnMap.length; i++) {
                result[i-1] = getMappedColumn(current, i);
            }
            return result;
        }
    }

    public String getMappedColumn(String[] array, int index) {
        if (columnMap == null) {
            return safeArrayGet(array, index);
        } else if (columnMap.length <= index) {
            return null;
        } else {
            return safeArrayGet(array, columnMap[index]);
        }
    }

    public String safeArrayGet(String[] array, int index) {
        if (array.length <= index ) {
            return null;
        } else {
            return array[index];
        }
    }

    //--------------------------------------------------------------------------
    // Assert methods
    //--------------------------------------------------------------------------

    public void assert_eq(final String... args) {
        if (!Arrays.equals(args, values)) {
            System.out.println("ASSERTION FAILED on value: " + StringUtils.join(values, ' '));
        }
    }

    public void assert_defined(String... args) {
        if (values == null) {
            System.out.println("ASSERTION FAILED");
        }
    }

    public void assert_undefined(String... args) {
        if (values != null) {
            System.out.println("ASSERTION FAILED on value: " + StringUtils.join(values, ' '));
        }
    }

    public void assert_empty(String... args) {
        String joinedValues = StringUtils.join(values, ' ');
        if (StringUtils.isNotEmpty(joinedValues)) {
            System.out.println("ASSERTION FAILED on value: " + joinedValues);
        }
    }

    public void assert_not_empty(String... args) {
        String joinedValues = StringUtils.join(values, ' ');
        if (StringUtils.isEmpty(joinedValues)) {
            System.out.println("ASSERTION FAILED on value: " + joinedValues);
        }
    }

    public void assert_contains(String... args) {
        Boolean success = true;
        for (String arg : args) {
            if (!ArrayUtils.contains(values, arg)) {
                success = false;
            }
        }
        if (!success) {
            System.out.println("ASSERTION FAILED on value: " + StringUtils.join(values, ' '));
        }
    }

    public void assert_not_contains(String... args) {
        Boolean success = true;
        for (String arg : args) {
            if (ArrayUtils.contains(values, arg)) {
                success = false;
            }
        }
        if (!success) {
            System.out.println("ASSERTION FAILED on value: " + StringUtils.join(values, ' '));
        }
    }

    public void assert_starts_with(String... args) {
        String joinedValues = StringUtils.join(values, ' ');
        String joinedArgs = StringUtils.join(values, ' ');
        if (!StringUtils.startsWith(joinedValues, joinedArgs)) {
            System.out.println("ASSERTION FAILED on value: " + joinedValues);
        }
    }

    public void assert_ends_with(String... args) {
        String joinedValues = StringUtils.join(values, ' ');
        String joinedArgs = StringUtils.join(values, ' ');
        if (!StringUtils.endsWith(joinedValues, joinedArgs)) {
            System.out.println("ASSERTION FAILED on value: " + joinedValues);
        }
    }


    //--------------------------------------------------------------------------
    // Abstract methods
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Getters/setters
    //--------------------------------------------------------------------------

}