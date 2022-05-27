
##### Requirements
- Java 1.8.x
- Maven

###### Packages
- [dyn4j-4.2.0](https://dyn4j.org/)
- [json-simple-4.0.1](https://cliftonlabs.github.io/json-simple/)


##### User Input

\<Space> pause/resume \
<W, A, S, D> apply up, left, down, right force to the bound object\
\<X> zero out the force\
<1, 2, 3, 4, 5> select object to move
Mouse click and drag moves the camera

##### World
```json
{
  "pixels_per_meter": 20, # Required Camera scaling critical for position
  "vehicles": [           # Required Array
    {
      "name": "Sample.Callie",  # To load a class need full package name
      "position": [-5,-5],      # optional, default to random
      "draw_scan_lines": "true" # optional, default to false
    },{
      "name": "Marie",          # To load JSON need path to file
      "position": [5,5],
      "draw_scan_lines": "true"
    }
  ],
  "lights": [                # Required Array
    {
      "position": [18,15],   # Required
      "bound_key": 1         # Optional, default to unbound
    }, {
      "position": [-18.0, -15.0]
    }
  ],
  "obstacles": [             # Optional Array
    {
      "position": [-10,-10], # Obstacle's center
      "size": [2,4]          # Width & Height
      "bound_key": 2         # Optional, default to unbound
    }
}
```
###### Details

Windows size is hard coded to 1600x800. The World is restricted to the Window size.

Camera has a pixels per meter (_ppm_) scale factor.

Therefore, object locations are restricted into the range:

[{-1600/_ppm_..1600/_ppm_},{-800/_ppm_..800/_ppm_}]

Key binding for light and obstacle moving is limited to 1 through 5.

Vehicles
```json
{
	"vehicleName": "Marie",       # Required
	"color": [188, 236, 51],      # Optional, default CYAN
	"behaviorTree": [             # Required
		{                         # Names must be full package paths
			"name": "behaviorFramework.arbiters.SimplePriority",
			"arbiter": true,      # This is an arbiter and has a sub-tree
			"weights": [ 0.3, 0.7 ],
			"behaviorTree": [
				{
					"name": "Sample.behaviors.Love"
				},
				{
					"name": "behaviorFramework.behaviors.Wander"
				} ,
				{
					"name": "Sample.behaviors.AvoidObstacle",
					"arbiter": false    # Optional for behaviors only required for arbiters
				},
				{
					"name": "behaviorFramework.behaviors.NoOp",
					"parameters": ["6", "10.3", "a string"] # Optional
				}
			]
		}
	]
}
```

If a Behavior has parameters, it will need to overload setParameters method
```java
public void setParameters(ArrayList<String> params) {
    super.setParameters(params);
    this.target = params.get(0);
}
```