package me.melonsboy.spwn.ide.themes;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;

import javax.swing.*;

public class flatIntelliJlaf extends Theme {
    private String xmlfile1 = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<!DOCTYPE RSyntaxTheme SYSTEM \"theme.dtd\">\n" +
            "\n" +
            "<!--\n" +
            "\tTheme that mimics IntelliJ IDEA's defaults.\n" +
            "\tSee theme.dtd and org.fife.ui.rsyntaxtextarea.Theme for more information.\n" +
            "-->\n" +
            "<RSyntaxTheme version=\"1.0\">\n" +
            "\n" +
            "   <!-- Omitting baseFont will use a system-appropriate monospaced. -->\n" +
            "   <!--<baseFont family=\"...\" size=\"13\"/>-->\n" +
            "   \n" +
            "   <!--  General editor colors. -->\n" +
            "   <background color=\"ffffff\" />\n" +
            "   <caret color=\"000000\" />\n" +
            "   <selection fg=\"ffffff\" bg=\"526da5\" />\n" +
            "   <currentLineHighlight color=\"ffffd7\" fade=\"false\" />\n" +
            "   <marginLine fg=\"b0b4b9\" />\n" +
            "   <markAllHighlight color=\"ccccff\" />\n" +
            "   <markOccurrencesHighlight color=\"ccccff\" border=\"false\" />\n" +
            "   <matchedBracket fg=\"99ccff\" bg=\"99ccff\" highlightBoth=\"true\" animate=\"false\" />\n" +
            "   <hyperlinks fg=\"0000ff\" />\n" +
            "   <secondaryLanguages>\n" +
            "      <language index=\"1\" bg=\"fff0cc\" />\n" +
            "      <language index=\"2\" bg=\"dafeda\" />\n" +
            "      <language index=\"3\" bg=\"ffe0f0\" />\n" +
            "   </secondaryLanguages>\n" +
            "\n" +
            "   <!-- Gutter styling. -->\n" +
            "   <gutterBorder color=\"dddddd\" />\n" +
            "   <lineNumbers fg=\"787878\" />\n" +
            "   <foldIndicator fg=\"808080\" iconBg=\"ffffff\" />\n" +
            "   <iconRowHeader activeLineRange=\"3399ff\" />\n" +
            "\n" +
            "   <!-- Syntax tokens. -->\n" +
            "   <tokenStyles>\n" +
            "      <style token=\"IDENTIFIER\" fg=\"000000\" />\n" +
            "      <style token=\"RESERVED_WORD\" fg=\"000080\" bold=\"true\" />\n" +
            "      <style token=\"RESERVED_WORD_2\" fg=\"000080\" bold=\"true\" />\n" +
            "      <style token=\"ANNOTATION\" fg=\"808000\" />\n" +
            "      <style token=\"COMMENT_DOCUMENTATION\" fg=\"808080\" italic=\"true\" />\n" +
            "      <style token=\"COMMENT_EOL\" fg=\"808080\" italic=\"true\" />\n" +
            "      <style token=\"COMMENT_MULTILINE\" fg=\"808080\" italic=\"true\" />\n" +
            "      <style token=\"COMMENT_KEYWORD\" fg=\"808080\" bold=\"true\" underline=\"true\" italic=\"true\" />\n" +
            "      <style token=\"COMMENT_MARKUP\" fg=\"808080\" bg=\"e2ffe2\" italic=\"true\" />\n" +
            "      <style token=\"DATA_TYPE\" fg=\"000080\" bold=\"true\" />\n" +
            "      <style token=\"FUNCTION\" fg=\"000000\" />\n" +
            "      <style token=\"LITERAL_BOOLEAN\" fg=\"000080\" bold=\"true\" />\n" +
            "      <style token=\"LITERAL_NUMBER_DECIMAL_INT\" fg=\"0000ff\" />\n" +
            "      <style token=\"LITERAL_NUMBER_FLOAT\" fg=\"0000ff\" />\n" +
            "      <style token=\"LITERAL_NUMBER_HEXADECIMAL\" fg=\"0000ff\" />\n" +
            "      <style token=\"LITERAL_STRING_DOUBLE_QUOTE\" fg=\"008000\" bold=\"true\" />\n" +
            "      <style token=\"LITERAL_CHAR\" fg=\"008000\" bold=\"true\" />\n" +
            "      <style token=\"LITERAL_BACKQUOTE\" fg=\"008000\" bold=\"true\" />\n" +
            "      <style token=\"MARKUP_TAG_DELIMITER\" fg=\"000000\" bold=\"true\" />\n" +
            "      <style token=\"MARKUP_TAG_NAME\" fg=\"000080\" bold=\"true\" />\n" +
            "      <style token=\"MARKUP_TAG_ATTRIBUTE\" fg=\"0000ff\" bold=\"true\" />\n" +
            "      <style token=\"MARKUP_TAG_ATTRIBUTE_VALUE\" fg=\"008000\" bold=\"true\" />\n" +
            "      <style token=\"MARKUP_COMMENT\" fg=\"808080\" italic=\"true\"/>\n" +
            "      <style token=\"MARKUP_DTD\" fg=\"808080\"/>\n" +
            "      <style token=\"MARKUP_PROCESSING_INSTRUCTION\"  fg=\"808080\"/>\n" +
            "      <style token=\"MARKUP_CDATA\" fg=\"cc6600\"/>\n" +
            "      <style token=\"MARKUP_CDATA_DELIMITER\" fg=\"008080\"/>\n" +
            "      <style token=\"MARKUP_ENTITY_REFERENCE\" fg=\"008000\"/>\n" +
            "      <style token=\"OPERATOR\" fg=\"000000\" />\n" +
            "      <style token=\"PREPROCESSOR\" fg=\"808080\" />\n" +
            "      <style token=\"REGEX\" fg=\"008040\" />\n" +
            "      <style token=\"SEPARATOR\" fg=\"000000\" />\n" +
            "      <style token=\"VARIABLE\" fg=\"810ca8\" bold=\"true\" />\n" +
            "      <style token=\"WHITESPACE\" fg=\"000000\" />\n" +
            "\n" +
            "      <style token=\"ERROR_IDENTIFIER\" fg=\"ff0000\" />\n" +
            "      <style token=\"ERROR_NUMBER_FORMAT\" fg=\"ff0000\" />\n" +
            "      <style token=\"ERROR_STRING_DOUBLE\" fg=\"ff0000\" />\n" +
            "      <style token=\"ERROR_CHAR\" fg=\"ff0000\" />\n" +
            "   </tokenStyles>\n" +
            "\n" +
            "</RSyntaxTheme>\n";
    public flatIntelliJlaf() {
        lookAndFeel = new FlatIntelliJLaf();
    }

    @Override
    public String toString() {
        return get_theme_name();
    }

    private final LookAndFeel lookAndFeel;
    @Override
    public LookAndFeel get_look_and_feel() {
        return lookAndFeel;
    }

    @Override
    public String get_rsyntaxtextarea_theme() {
        return xmlfile1;
    }

    @Override
    public String get_theme_name() {
        return "FlatIntelliJLaf";
    }
}
