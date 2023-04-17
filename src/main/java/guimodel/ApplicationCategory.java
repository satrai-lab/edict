package guimodel;

public class ApplicationCategory {

	private String categoryId;
	
	private String categoryName;

	public ApplicationCategory() {
		super();
	}
	public ApplicationCategory(String categoryId) {
		super();
		this.categoryId = categoryId;
	}

	public ApplicationCategory(String categoryId, String categoryName) {
		super();
		this.categoryId = categoryId;
		this.categoryName = categoryName;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	
	
	
}