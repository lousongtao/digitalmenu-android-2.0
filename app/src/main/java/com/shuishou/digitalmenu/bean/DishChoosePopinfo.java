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
	
	@Column("popinfo_cn")
	private String popInfoCN;
	
	@Column("popinfo_en")
	private String popInfoEN;

	@Column("dish_id")
	@Mapping(Relation.OneToOne)
	private Dish dish;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}


	public String getPopInfoCN() {
		return popInfoCN;
	}

	public void setPopInfoCN(String popInfoCN) {
		this.popInfoCN = popInfoCN;
	}

	public String getPopInfoEN() {
		return popInfoEN;
	}

	public void setPopInfoEN(String popInfoEN) {
		this.popInfoEN = popInfoEN;
	}

	public Dish getDish() {
		return dish;
	}

	public void setDish(Dish dish) {
		this.dish = dish;
	}

	@Override
	public String toString() {
		return "DishChoosePopinfo [id=" + id + ", dish=" + dish.getChineseName() + ", popInfoCN=" + popInfoCN + "]";
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
