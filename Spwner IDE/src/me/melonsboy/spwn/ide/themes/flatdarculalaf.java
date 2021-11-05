package me.melonsboy.spwn.ide.themes;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class flatdarculalaf extends Theme {
    private String xmlfile1 = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<!DOCTYPE RSyntaxTheme SYSTEM \"theme.dtd\">\n" +
            "\n" +
            "<!--\n" +
            "\tDark theme based off of Notepad++'s Obsidian theme.\n" +
            "\tSee theme.dtd and org.fife.ui.rsyntaxtextarea.Theme for more information.\n" +
            "-->\n" +
            "<RSyntaxTheme version=\"1.0\">\n" +
            "\n" +
            "\t<!-- Omitting baseFont will use a system-appropriate monospaced. -->\n" +
            "\t<!--<baseFont family=\"...\" size=\"13\"/>-->\n" +
            "\n" +
            "\t<!--  General editor colors. -->\n" +
            "\t<background color=\"293134\" />\n" +
            "\t<caret color=\"c1cbc2\" />\n" +
            "\t<selection useFG=\"false\" bg=\"404E51\" roundedEdges=\"false\" />\n" +
            "\t<currentLineHighlight color=\"2F393C\" fade=\"false\" />\n" +
            "\t<marginLine fg=\"394448\" />\n" +
            "\t<markAllHighlight color=\"6b8189\" />\n" +
            "\t<!-- TODO: Fix me -->\n" +
            "\t<markOccurrencesHighlight color=\"5b7179\" border=\"false\" />\n" +
            "\t<matchedBracket fg=\"6A8088\" bg=\"6b8189\" highlightBoth=\"false\" animate=\"true\" />\n" +
            "\t<hyperlinks fg=\"a082bd\" />\n" +
            "\t<secondaryLanguages>\n" +
            "\t\t<language index=\"1\" bg=\"333344\" />\n" +
            "\t\t<language index=\"2\" bg=\"223322\" />\n" +
            "\t\t<language index=\"3\" bg=\"332222\" />\n" +
            "\t</secondaryLanguages>\n" +
            "\n" +
            "\t<!-- Gutter styling. -->\n" +
            "\t<gutterBorder color=\"81969A\" />\n" +
            "\t<lineNumbers fg=\"81969A\" />\n" +
            "\t<foldIndicator fg=\"6A8088\" iconBg=\"2f383c\" iconArmedBg=\"3f484c\" />\n" +
            "\t<iconRowHeader activeLineRange=\"3399ff\" />\n" +
            "\n" +
            "\t<!-- Syntax tokens. -->\n" +
            "\t<tokenStyles>\n" +
            "\t\t<style token=\"IDENTIFIER\" fg=\"a9b7c6\" />\n" +
            "\t\t<style token=\"RESERVED_WORD\" fg=\"93C763\" bold=\"true\" />\n" +
            "\t\t<style token=\"RESERVED_WORD_2\" fg=\"93C763\" bold=\"true\" />\n" +
            "\t\t<style token=\"ANNOTATION\" fg=\"E8E2B7\" />\n" +
            "\t\t<style token=\"COMMENT_DOCUMENTATION\" fg=\"6C788C\" />\n" +
            "\t\t<style token=\"COMMENT_EOL\" fg=\"66747B\" />\n" +
            "\t\t<style token=\"COMMENT_MULTILINE\" fg=\"66747B\" />\n" +
            "\t\t<style token=\"COMMENT_KEYWORD\" fg=\"ae9fbf\" />\n" +
            "\t\t<style token=\"COMMENT_MARKUP\" fg=\"ae9fbf\" />\n" +
            "\t\t<style token=\"FUNCTION\" fg=\"E0E2E4\" />\n" +
            "\t\t<style token=\"DATA_TYPE\" fg=\"678CB1\" bold=\"true\" />\n" +
            "\t\t<style token=\"LITERAL_BOOLEAN\" fg=\"93C763\" bold=\"true\" />\n" +
            "\t\t<style token=\"LITERAL_NUMBER_DECIMAL_INT\" fg=\"FFCD22\" />\n" +
            "\t\t<style token=\"LITERAL_NUMBER_FLOAT\" fg=\"FFCD22\" />\n" +
            "\t\t<style token=\"LITERAL_NUMBER_HEXADECIMAL\" fg=\"FFCD22\" />\n" +
            "\t\t<style token=\"LITERAL_STRING_DOUBLE_QUOTE\" fg=\"EC7600\" />\n" +
            "\t\t<style token=\"LITERAL_CHAR\" fg=\"EC7600\" />\n" +
            "\t\t<style token=\"LITERAL_BACKQUOTE\" fg=\"EC7600\" />\n" +
            "\t\t<style token=\"MARKUP_TAG_DELIMITER\" fg=\"678CB1\" />\n" +
            "\t\t<style token=\"MARKUP_TAG_NAME\" fg=\"ABBFD3\" bold=\"true\" />\n" +
            "\t\t<style token=\"MARKUP_TAG_ATTRIBUTE\" fg=\"B3B689\" />\n" +
            "\t\t<style token=\"MARKUP_TAG_ATTRIBUTE_VALUE\" fg=\"e1e2cf\" />\n" +
            "\t\t<style token=\"MARKUP_COMMENT\" fg=\"66747B\" />\n" +
            "\t\t<style token=\"MARKUP_DTD\" fg=\"A082BD\" />\n" +
            "\t\t<style token=\"MARKUP_PROCESSING_INSTRUCTION\" fg=\"A082BD\" />\n" +
            "\t\t<style token=\"MARKUP_CDATA\" fg=\"d5e6f0\" />\n" +
            "\t\t<style token=\"MARKUP_CDATA_DELIMITER\" fg=\"ae9fbf\" />\n" +
            "\t\t<style token=\"MARKUP_ENTITY_REFERENCE\" fg=\"678CB1\" />\n" +
            "\t\t<style token=\"OPERATOR\" fg=\"E8E2B7\" />\n" +
            "\t\t<style token=\"PREPROCESSOR\" fg=\"A082BD\" />\n" +
            "\t\t<style token=\"REGEX\" fg=\"d39745\" />\n" +
            "\t\t<style token=\"SEPARATOR\" fg=\"E8E2B7\" />\n" +
            "\t\t<style token=\"VARIABLE\" fg=\"ae9fbf\" bold=\"true\" />\n" +
            "\t\t<style token=\"WHITESPACE\" fg=\"E0E2E4\" />\n" +
            "\n" +
            "\t\t<style token=\"ERROR_IDENTIFIER\" fg=\"E0E2E4\" bg=\"04790e\" />\n" +
            "\t\t<style token=\"ERROR_NUMBER_FORMAT\" fg=\"E0E2E4\" bg=\"04790e\" />\n" +
            "\t\t<style token=\"ERROR_STRING_DOUBLE\" fg=\"E0E2E4\" bg=\"04790e\" />\n" +
            "\t\t<style token=\"ERROR_CHAR\" fg=\"E0E2E4\" bg=\"04790e\" />\n" +
            "\t</tokenStyles>\n" +
            "\n" +
            "</RSyntaxTheme>";
    public flatdarculalaf() {
        lookAndFeel = new FlatDarculaLaf();
    }

    @Override
    public String toString() {
        return get_theme_name();
    }

    private LookAndFeel lookAndFeel;
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
        return "FlatDarculaLaf";
    }
}
