package com.shuishou.digitalmenu.bean;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.Mapping;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.AssignType;
import com.litesuits.orm.db.enums.Relation;

import java.io.Serializable;

@Table("dishchoose_popinfo")
public class DishChoosePopinfo implements Serializable{

	@PrimaryKey(value = AssignType.BY_MYSELF)
	private int id;

	@Column("first_language_name")
	private String firstLanguageName;

	@Column("second_language_name")
	private String secondLanguageName;

	@Column("dish_id")
	@Mapping(Relation.OneToOne)
	private Dish dish;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFirstLanguageName() {
		return firstLanguageName;
	}

	public void setFirstLanguageName(String firstLanguageName) {
		this.firstLanguageName = firstLanguageName;
	}

	public String getSecondLanguageName() {
		return secondLanguageName;
	}

	public void setSecondLanguageName(String secondLanguageName) {
		this.secondLanguageName = secondLanguageName;
	}

	public Dish getDish() {
		return dish;
	}

	public void setDish(Dish dish) {
		this.dish = dish;
	}

	@Override
	public String toString() {
		return "DishChoosePopinfo{" +
				"firstLanguageName='" + firstLanguageName + '\'' +
				'}';
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DishChoosePopinfo other = (DishChoosePopinfo) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	
}
