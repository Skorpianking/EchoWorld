
###### Requirements
- Java 1.8.x
- Maven

###### Packages
- [dyn4j-4.2.0](https://dyn4j.org/)
- [json-simple-4.0.1](https://cliftonlabs.github.io/json-simple/)


###### User Input 

\<Space> pause/resume \
<W, A, S, D> apply up, left, down, right force to the bound object\
\<X> zero out the force\
<1, 2, 3, 4, 5> select object to move
Mouse click and drag moves the camera

###### Details

Windows size is hard coded to 1600x800. The World is restricted to the Window size.

Camera has a pixels per meter (_ppm_) scale factor.

Therefore, object locations are restricted into the range:

[{-1600/_ppm_..1600/_ppm_},{-800/_ppm_..800/_ppm_}]
