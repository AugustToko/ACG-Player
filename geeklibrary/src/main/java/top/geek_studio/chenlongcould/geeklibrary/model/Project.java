package top.geek_studio.chenlongcould.geeklibrary.model;

/**
 * project
 *
 * @author : chenlongcould
 * @date : 2019/06/08/17
 */
public class Project {

	private String name;

	private String infoUrl;

	private String releaseUrl;

	private String homePage;

	public Project(String name, String infoUrl, String releaseUrl, String homePage) {
		this.name = name;
		this.infoUrl = infoUrl;
		this.releaseUrl = releaseUrl;
		this.homePage = homePage;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getInfoUrl() {
		return infoUrl;
	}

	public void setInfoUrl(String infoUrl) {
		this.infoUrl = infoUrl;
	}

	public String getReleaseUrl() {
		return releaseUrl;
	}

	public void setReleaseUrl(String releaseUrl) {
		this.releaseUrl = releaseUrl;
	}

	public String getHomePage() {
		return homePage;
	}

	public void setHomePage(String homePage) {
		this.homePage = homePage;
	}
}
