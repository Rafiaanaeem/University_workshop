import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

public class ProjectListScreen extends JFrame {

    private User currentUser;

    private final Font MAIN_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private final Color THEME_COLOR = new Color(70, 130, 180); 
    private final Color BACK_BTN_COLOR = new Color(220, 20, 60); 

    private JPanel listContainer;

    public ProjectListScreen(User user) {
        this.currentUser = user;

        setTitle("University Workshop - Projects");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(550, 650); 
        setLocationRelativeTo(null); 
        setLayout(new BorderLayout());

        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(new EmptyBorder(15, 0, 15, 0)); 
        JLabel titleLabel = new JLabel("Projects");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(THEME_COLOR);
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JButton btnBack = new JButton("Back");
        styleButton(btnBack, BACK_BTN_COLOR);
        
        btnBack.addActionListener(e -> {
            dispose(); 
            new MenuScreen(currentUser).setVisible(true); 
        });
        bottomPanel.add(btnBack);
        add(bottomPanel, BorderLayout.SOUTH);

        loadProjectData();
    }

    private void loadProjectData() {
        listContainer.removeAll();

        List<Application> projects = Application.getProjectList();

        if (projects.isEmpty()) {
            JLabel lblEmpty = new JLabel("No active projects found.");
            lblEmpty.setFont(MAIN_FONT);
            lblEmpty.setAlignmentX(Component.CENTER_ALIGNMENT);
            listContainer.add(lblEmpty);
        } else {
            for (Application app : projects) {
                addProjectRow(app);
            }
        }
        
        listContainer.revalidate();
        listContainer.repaint();
    }

    private void addProjectRow(Application app) {
        // Use GridBagLayout for vertical centering
        JPanel rowPanel = new JPanel(new GridBagLayout()); 
        rowPanel.setBackground(Color.WHITE);
        
        rowPanel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(10, 20, 5, 20), 
                new LineBorder(THEME_COLOR, 2, true) 
        ));
        
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90)); 

        GridBagConstraints gbc = new GridBagConstraints();

        JLabel lblName = new JLabel(" " + app.getProjName());
        lblName.setFont(MAIN_FONT);
        lblName.setForeground(Color.BLACK); 
        
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1.0; 
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST; 
        rowPanel.add(lblName, gbc);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonsPanel.setBackground(Color.WHITE);

        JButton btnDetails = new JButton("Details");
        styleButton(btnDetails, THEME_COLOR);
        btnDetails.addActionListener(e -> {
            DetailsDialog dialog = new DetailsDialog(this, app.getProjName());
            dialog.setVisible(true);
        });

        JButton btnProgress = new JButton("Progress");
        styleButton(btnProgress, THEME_COLOR);
        btnProgress.addActionListener(e -> {
            ProgressDialog dialog = new ProgressDialog(this, app.getProjName());
            dialog.setVisible(true);
        });

        buttonsPanel.add(btnDetails);
        buttonsPanel.add(btnProgress);

        gbc.gridx = 1; gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER; 
        rowPanel.add(buttonsPanel, gbc);

        listContainer.add(rowPanel);
        listContainer.add(Box.createRigidArea(new Dimension(0, 5)));
    }

    private void styleButton(JButton btn, Color bgColor) {
        btn.setFont(MAIN_FONT); 
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false); 
        btn.setOpaque(true);         
        btn.setBorder(new EmptyBorder(8, 20, 8, 20)); 
    }
}