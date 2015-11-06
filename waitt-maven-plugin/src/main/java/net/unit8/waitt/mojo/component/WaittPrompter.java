package net.unit8.waitt.mojo.component;

import org.apache.maven.plugins.annotations.Component;
import org.codehaus.plexus.components.interactivity.InputHandler;
import org.codehaus.plexus.components.interactivity.OutputHandler;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author kawasima
 */
public class WaittPrompter implements Prompter {
    @Component
    private OutputHandler outputHandler;
    
    @Component
    private InputHandler inputHandler;

    @Override
    public String prompt(String message) throws PrompterException {
        writePrompt(message);
        return readLine();
    }

    @Override
    public String prompt(String message, String defaultReply) throws PrompterException {
        writePrompt(message);
        String line = readLine();
        if (line == null || line.isEmpty()) {
            line = defaultReply;
        }
        return line;
    }

    @Override
    public String prompt(String message, List possibleValues) throws PrompterException {
        return prompt(message, possibleValues, null);
    }

    @Override
    public String prompt(String message, List possibleValues, String defaultReply) throws PrompterException {
        String line;
        do {
            writePrompt(message);
            line = readLine();
            if (line == null || line.isEmpty()) {
                line = defaultReply;
            }
            if (line != null && !possibleValues.contains(line)) {
                try {
                    outputHandler.writeLine("Invalid selection.");
                } catch(IOException e) {
                    throw new PrompterException("Failed to present feedback", e);
                }
            }
        } while(line == null || !possibleValues.contains(line));
        
        return line;
    }

    @Override
    public String promptForPassword(String message) throws PrompterException {
        writePrompt(message);
        
        try {
            return inputHandler.readPassword();
        } catch(IOException e) {
            throw new PrompterException("Failed to read password", e);
        }
    }

    
    @Override
    public void showMessage(String message) throws PrompterException {
        try {
            outputHandler.write(message);
        } catch (IOException e) {
            throw new PrompterException("Failed to show message", e);
        }
    }
    
    private String readLine() throws PrompterException {
        try {
            return inputHandler.readLine();
        } catch (IOException e) {
            throw new PrompterException("Failed to read line", e);
        }
    }
    
    private void writePrompt(String message) throws PrompterException {
        showMessage(message + ": ");
    }
    
}
