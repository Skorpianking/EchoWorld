package Vehicles;

import org.dyn4j.collision.TypeFilter;

public final class Categories {
  private static class Root extends TypeFilter {}
  private static class World extends Root {} // Vehicles, Obstacles, and Lights
  private static class Homes extends Root {}
  private static class Food extends World {}
  private static class Category4 extends World {}

  public static final TypeFilter ROOT = new Root();
  public static final TypeFilter HOMES = new Homes();
  public static final TypeFilter WORLD = new World();
  public static final TypeFilter FOOD = new Food();
  public static final TypeFilter CATEGORY4 = new Category4();
}
