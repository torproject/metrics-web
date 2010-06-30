package ernie;

public class TemplateController {

/*
 * TODO
 * Check if referenced template exists. If not,
 * set it to 404 page.
 */

/**
 * The main parent template and layout.
 */
    private String mainTemplate;

/**
 * The current template, the default is index on initialization
 */
    private String currentTemplate;

/**
 * This is referencing name.
 */
    private String templateName;

/**
 * The page title to be displayed.
 */
    private String title;

    public TemplateController()  {
        this.mainTemplate = "/WEB-INF/templates/main.tpl.jsp";
        this.currentTemplate = "/WEB-INF/templates/index.tpl.jsp";
        this.templateName = "index";
        this.title = "Tor Metrics Portal";
    }

/*
* Setters
*/
    public void setTemplate(String tpl) {
        this.templateName = tpl;
        this.currentTemplate = "/WEB-INF/templates/" + tpl + ".tpl.jsp";
    }

    public void setTitle(String title) {
        this.title=title;
    }

/*
* Getters
*/
    public String getTemplate() {
        return currentTemplate;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getTitle() {
        return title;
    }

    public String getMainTemplate() {
        return mainTemplate;
    }
}
