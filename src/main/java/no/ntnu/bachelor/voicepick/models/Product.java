package no.ntnu.bachelor.voicepick.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = Product.TABLE_NAME)
public class Product {

  public static final String TABLE_NAME = "product";
  public static final String PRIMARY_KEY = "product_id";

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = Product.PRIMARY_KEY)
  public Long id;

  @Column(name = "product_name")
  private String name;

  @JsonManagedReference
  @OneToOne
  @JoinColumn(name = Location.PRIMARY_KEY)
  private Location location;

  @Column(name = "weight")
  private double weight;

  @Column(name = "volume")
  private double volume;

  @Column(name = "quantity")
  private int quantity;

  @Column(name = "type")
  private ProductType type;

  @Column(name = "status")
  private Status status;

  public Product(String name, Location location, double weight, double volume, int quantity, ProductType type,
      Status status) {
    this.name = name;
    this.location = location;
    this.weight = weight;
    this.volume = volume;
    this.quantity = quantity;
    this.type = type;
    this.status = status;
  }

}