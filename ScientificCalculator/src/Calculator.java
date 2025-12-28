import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class Calculator {

    private JFrame frame;
    private JTextField display;
    private JLabel historyLabel;

    double first, second, result;
    String operator = "";
    String history = "";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Calculator().createUI());
    }

    private void createUI() {
        frame = new JFrame("Scientific Calculator");
        frame.setSize(420, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        // ===== TOP PANEL (Display & History) =====
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(new EmptyBorder(20, 20, 10, 20));

        // History label
        historyLabel = new JLabel(" ");
        historyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        historyLabel.setForeground(Color.GRAY);
        historyLabel.setHorizontalAlignment(JLabel.RIGHT);
        topPanel.add(historyLabel, BorderLayout.NORTH);

        // Display
        display = new JTextField("0");
        display.setFont(new Font("Segoe UI", Font.BOLD, 36));
        display.setHorizontalAlignment(JTextField.RIGHT);
        display.setEditable(false);
        display.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        topPanel.add(display, BorderLayout.CENTER);

        frame.add(topPanel, BorderLayout.NORTH);

        // ===== MAIN BUTTON PANEL =====
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 20, 20, 20));

        // ===== SCIENTIFIC PANEL =====
        JPanel sciPanel = new JPanel(new GridLayout(2, 6, 8, 8));
        String[] sciButtons = {
                "sin", "cos", "tan", "√", "x²", "x³",
                "log", "ln", "eˣ", "xʸ", "n!", "1/x"
        };

        for (String txt : sciButtons) {
            JButton btn = createSciButton(txt);
            btn.addActionListener(e -> scientificAction(txt));
            sciPanel.add(btn);
        }

        mainPanel.add(sciPanel, BorderLayout.NORTH);

        // ===== BASIC PANEL =====
        JPanel basicPanel = new JPanel(new GridLayout(5, 4, 8, 8));
        String[] basicButtons = {
                "C", "<-", "%", "/",
                "7", "8", "9", "*",
                "4", "5", "6", "-",
                "1", "2", "3", "+",
                ".", "0", "=", "±"
        };

        for (String txt : basicButtons) {
            JButton btn = createBasicButton(txt);
            btn.addActionListener(e -> basicAction(txt));
            basicPanel.add(btn);
        }

        mainPanel.add(basicPanel, BorderLayout.CENTER);

        frame.add(mainPanel, BorderLayout.CENTER);

        // Add keyboard support
        setupKeyboardSupport();

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // ===== BUTTON FACTORY METHODS =====
    private JButton createSciButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Keep default colors but add a subtle border
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 2, 8, 2)));
        
        return btn;
    }

    private JButton createBasicButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Keep default colors, just add subtle hover effect
        Color originalColor = btn.getBackground();
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // Lighten the color slightly on hover
                btn.setBackground(blend(originalColor, Color.WHITE, 0.1f));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(originalColor);
            }
        });
        
        // Make equals button slightly different
        if (text.equals("=")) {
            btn.setFont(new Font("Segoe UI", Font.BOLD, 20));
            btn.setForeground(Color.WHITE);
            btn.setBackground(new Color(0, 120, 215)); // Windows blue
        } else if (text.matches("[C⌫%]")) {
            btn.setForeground(Color.RED.darker());
        } else if (text.matches("[/*\\-+]")) {
            btn.setForeground(new Color(0, 100, 0)); // Dark green
        }
        
        return btn;
    }

    // Helper method to blend colors
    private Color blend(Color color1, Color color2, float ratio) {
        if (ratio > 1f) ratio = 1f;
        else if (ratio < 0f) ratio = 0f;
        float iRatio = 1.0f - ratio;

        int i1 = color1.getRGB();
        int i2 = color2.getRGB();

        int a1 = (i1 >> 24 & 0xff);
        int r1 = ((i1 & 0xff0000) >> 16);
        int g1 = ((i1 & 0xff00) >> 8);
        int b1 = (i1 & 0xff);

        int a2 = (i2 >> 24 & 0xff);
        int r2 = ((i2 & 0xff0000) >> 16);
        int g2 = ((i2 & 0xff00) >> 8);
        int b2 = (i2 & 0xff);

        int a = (int)((a1 * iRatio) + (a2 * ratio));
        int r = (int)((r1 * iRatio) + (r2 * ratio));
        int g = (int)((g1 * iRatio) + (g2 * ratio));
        int b = (int)((b1 * iRatio) + (b2 * ratio));

        return new Color(a << 24 | r << 16 | g << 8 | b);
    }

    // ===== BASIC BUTTON LOGIC =====
    private void basicAction(String cmd) {
        String currentText = display.getText();

        switch (cmd) {
            case "C":
                display.setText("0");
                operator = "";
                history = "";
                historyLabel.setText(" ");
                break;

            case "⌫":
                if (currentText.length() > 1) {
                    display.setText(currentText.substring(0, currentText.length() - 1));
                } else {
                    display.setText("0");
                }
                break;

            case "±":
                if (!currentText.equals("0")) {
                    if (currentText.startsWith("-")) {
                        display.setText(currentText.substring(1));
                    } else {
                        display.setText("-" + currentText);
                    }
                }
                break;

            case "%":
                double percent = Double.parseDouble(currentText) / 100;
                display.setText(format(percent));
                break;

            case "/":
            case "*":
            case "-":
            case "+":
                first = Double.parseDouble(currentText);
                operator = cmd;
                history = format(first) + " " + operator;
                historyLabel.setText(history);
                display.setText("");
                break;

            case "=":
                if (!operator.isEmpty()) {
                    second = Double.parseDouble(currentText);
                    calculate();
                    history = format(first) + " " + operator + " " + format(second) + " =";
                    historyLabel.setText(history);
                    operator = "";
                }
                break;

            default: // Numbers and decimal point
                if (currentText.equals("0") && !cmd.equals(".")) {
                    display.setText(cmd);
                } else {
                    if (cmd.equals(".") && currentText.contains(".")) {
                        return; // Prevent multiple decimal points
                    }
                    display.setText(currentText + cmd);
                }
        }
    }

    // ===== SCIENTIFIC BUTTON LOGIC =====
    private void scientificAction(String cmd) {
        double val = Double.parseDouble(display.getText());
        String operation = "";
        
        switch (cmd) {
            case "sin": 
                result = Math.sin(Math.toRadians(val));
                operation = "sin(" + format(val) + ")";
                break;
            case "cos": 
                result = Math.cos(Math.toRadians(val));
                operation = "cos(" + format(val) + ")";
                break;
            case "tan": 
                result = Math.tan(Math.toRadians(val));
                operation = "tan(" + format(val) + ")";
                break;
            case "√":   
                result = Math.sqrt(val);
                operation = "√(" + format(val) + ")";
                break;
            case "x²":  
                result = val * val;
                operation = "(" + format(val) + ")²";
                break;
            case "x³":  
                result = val * val * val;
                operation = "(" + format(val) + ")³";
                break;
            case "log": 
                result = Math.log10(val);
                operation = "log(" + format(val) + ")";
                break;
            case "ln":  
                result = Math.log(val);
                operation = "ln(" + format(val) + ")";
                break;
            case "eˣ":  
                result = Math.exp(val);
                operation = "e^(" + format(val) + ")";
                break;
            case "n!":  
                result = factorial(val);
                operation = format(val) + "!";
                break;
            case "1/x": 
                result = 1 / val;
                operation = "1/(" + format(val) + ")";
                break;
            case "xʸ":
                first = val;
                operator = "xʸ";
                history = format(first) + " ^ ";
                historyLabel.setText(history);
                display.setText("");
                return;
        }
        historyLabel.setText(operation + " =");
        display.setText(format(result));
    }

    // ===== CALCULATION =====
    private void calculate() {
        switch (operator) {
            case "+": result = first + second; break;
            case "-": result = first - second; break;
            case "*": result = first * second; break;
            case "/": result = first / second; break;
            case "xʸ": result = Math.pow(first, second); break;
        }
        display.setText(format(result));
    }

    // ===== UTILITIES =====
    private double factorial(double n) {
        if (n < 0 || n > 20) return 0; // Prevent overflow
        double f = 1;
        for (int i = 1; i <= (int)n; i++) f *= i;
        return f;
    }

    private String format(double val) {
        if (val == (long)val) {
            return String.format("%d", (long)val);
        } else {
            // Remove trailing zeros
            String formatted = String.format("%.10f", val);
            formatted = formatted.replaceAll("0*$", "").replaceAll("\\.$", "");
            return formatted;
        }
    }

    // ===== KEYBOARD SUPPORT =====
    private void setupKeyboardSupport() {
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                char keyChar = e.getKeyChar();
                int keyCode = e.getKeyCode();
                
                if (keyChar >= '0' && keyChar <= '9') {
                    basicAction(String.valueOf(keyChar));
                } else if (keyChar == '.') {
                    basicAction(".");
                } else if (keyChar == '+') {
                    basicAction("+");
                } else if (keyChar == '-') {
                    basicAction("-");
                } else if (keyChar == '*') {
                    basicAction("*");
                } else if (keyChar == '/') {
                    basicAction("/");
                } else if (keyCode == KeyEvent.VK_ENTER) {
                    basicAction("=");
                } else if (keyCode == KeyEvent.VK_ESCAPE) {
                    basicAction("C");
                } else if (keyCode == KeyEvent.VK_BACK_SPACE) {
                    basicAction("⌫");
                }
            }
        });
        frame.setFocusable(true);
        frame.requestFocus();
    }
}