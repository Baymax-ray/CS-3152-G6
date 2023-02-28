1. BASIC PHYSIC QUESTIONS
    What is Newton’s second law of motion? Do not just write F = ma; explain the intuition
    behind it.
        Newton's second law of motion, F = ma, describes the changes that force produces
        on the motion of a body. A larger force means greater acceleration. A larger mass
        needs more force to give it acceleration compared to an object with smaller mass.
        A larger acceleration requires a high force to mass ratio.
    What is momentum? Impulse?
        Momentum describes the idea that a mass in motion tends to stay in motion in the
        same direction and at the same velocity unless it faces outside forces. Momentum
        is represented by the equation by p = m * v. An object's momentum is proportional
        to mass and velocity of the object.

        Impulse measures the changes in momentum that happen when a outside force is
        applied to the object. It is represented by the equation J = delta(p) = F * delta
        (t) where F is force and t is the time interval over which the force is applied.
        It is measured in newton-seconds (N*s). The larger the force, the larger the
        change in momentum. The longer a force is applied, the larger the change in
        momentum.
    What is a perfectly elastic collision?
        A perfectly elastic collision is a collision in which the total kinetic energy of
        two colliding objects is conserved, meaning the objects bounce off each other with
        the same total magnitude of kinetic energy as they had before. No energy is lost
        as sound, friction, light, heat, etc. This does not happen in the real world.
    Give a real-life example of a (nearly) perfectly elastic collision.
        Rubber balls bouncing into each other in the vacuum of outer space would have a
        nearly elastic collision because there is not much friction and few outside forces.
    What is a perfectly inelastic collision?
        A perfectly inelastic collision is a collision where to objects stick to each other
        and move as a single object after the collision.
    Give a real-life example of a (nearly) perfectly inelastic collision.
        When two cars crash and the bumpers become interlocked and they move in the same
        direction is a perfectly inelastic collision. When a sticky ball is thrown against a
        wall and sticks to it.
    What is the coefficient of restitution?
        The coefficient of restitution measures the elasticity of a collision. It is
        calculated by the equation e = (relative velocity after collision) /
        (relative velocity before collision), which is the ratio of relative velocity of
        separation after the collision compared to before the collision.
    What range of values can it take?
        e = 1 for a perfectly elastic collision and e = 0 for a perfectly inelastic
        equation. The value can range between these two values depending on how bouncy the
        equation is.
    What is angular velocity? Angular acceleration?
        Angular velocity describes how fast a rigid body rotes in respect to its center of
        rotation (some fixed point or axis) in radians per second (rad/s). Its equation is
        omega = v / r = 2 pi f = change in angle / change in time, where r is radius, f is
        frequency in revolutions per sec, and v is velocity. The direction of angular
        velocity is always perpendicular to the plane of rotation and is proportional to
        velocity and distance from axis of rotation. Angular acceleration measures change in
        angular velocity over time in radians per second squares (rad/sec^2). Basically,
        it measures how quickly an object is changing its angular velocity (is it rotating
        faster or slower than before?).
    What is the moment of inertia?
        Moment of inertia measures an object's resistance to angular acceleration. High
        moments of inertia mean that it requires more force to change that object's
        angular velocity. The equation for moment of inertia is dependent on the shape of
        the object, but generally is dependent on mass and average distance from axis
        of rotation, so that objects with more mass away from the axis of rotation
         have higher moments of inertia. It is measured in kg m^2.
    Angular momentum?
        Angular momentum measures the product of  moment of inertia and angular velocity
        and is a rotational analog of linear momentum. In a closed system without externa
        torque, the total angular momentum remain constant.
    What is torque?
        Torque is the rotational analog of force. It measures the force that causes
        changes in angular acceleration around an axis of rotation. It is equal to
        force * distance between axis of rotation and the point where the force is applied.
    What is the relationship between torque, moment of inertia, and angular acceleration?
        Torque = moment of inertia * angular acceleration. This is the rotational analog
        of Newton's second law of motion. The acceleration of an object is proportional
        to the ration of torque to moment of inertia.

BOX2D QUESTIONS
    In Box2d, what is the difference between a Shape and a Body?
        At the core, shapes just describe the collision geometry of an object whereas
        bodies describe rigid bodies that are simulated in the physics engine. Shapes
        can be attached to a body to define the physical shape of that body. Bodies
        have position and velocity. You can apply forces, torques, and impulses to
        said bodies to change the position and velocity. Bodies are the backbone
        for shapes and can move them around the world.
    Between a Shape and a Body, which do you go to change a physical property? To
    apply a force?
        The physical properties of an object are simulated in the physics engine with
        the body. Shapes only define the collision geometry and do not affect its
        physical properties directly. Forces, torques, and impulses can be applied to
        change the physical properties like velocity and position.
    When would you want a Body to contain multiple Shapes?
        Multiple shapes can be used to represent the collision for an object with a more
        irregular shapes that can't be represented by a single polygon. For example,
        if you want to make the collision for a monster truck, you might attach 2
        circle shapes to a polygonal body. Breaking a really complex polygon object up into
        multiple shapes can also help with performance.
    In Box2d, what is a World? What are some of its important properties?
        Every Box2D programs begins with the creation of a b2World object, which is the
        physics hub that manages memory, objects, and simulation. Its most important property
        is its gravity vector which is a parameter in the creation of a world object. This
        defines the force and power of gravity in a world. It also usually has a ground body
        and ground shape defined which defines the ground's shape, friction, and density.
    What is the advantage of sleeping an object with a Body? When would you want to
    do this?
        Simulating physics bodies is expensive, so we should try to reduce to the total
        amount of physics bodies. When Box2D determines that a body is at rest (no more
        acceleration), it will put the body to sleep, which means physics calculations are no
        longer being made for the object. Bodies will wake up if some other body collides with
        it or if a joint attached to them is destroyed. Bodies can be put to sleep or woken up
        manually.
    In Box2d, what is the difference between a static body and a dynamic body? How
    do you specify which type a body is?
        A static body does not move while it is being simulated because its mass is set
        to infinite. Static bodies can be moved manually, but they have zero velocity and do
        not collide with other static or kinematic bodies. Dynamic bodies can collide with
        static bodies, but they won't be able to move them.
    In Box2d, what is a Bullet and when do you want an object to be one?
        Fast moving objects can cause issues with physics simulation in Box2D with continuous
        collision detection which prevents dynamic objects from tunneling through static or
        dynamic bodies. CCD it normally no used between dynamic bodies, but objects can be
        labelled as bullets to turn on CCD between dynamic bodies for this object. This
        prevents a fast-moving object from tunnelling through other dynamic bodies by permantly
        turning on CCD.
    In LibGDX, what can you do with a ContactListener inside a World? How does this
    help with sensors?
        A contact listener receives contact data from 2 fixtures. It can listen for contact
        beginning, ending, before the solver starts, or after the solver ends. Code can then
        run based off when these events are triggered.
    In LibGDX, what are the steps that you must take to add a Shape to a Body?
        You must create a new BodyDef object with a body type (static, dynamic, kinematic)
        and a starting position. You then must create a body object in the world from this
        body definition. After create a shape. Then, create a fixtureDef with the given
        shape, some density, some friction, and some restitution. Then, add the FixtureDef
        to the body and dispose of the shape after.

ROCKET
    Move The Rocket
        To move the rocket, I first read the input and set the force in the
        corresponding direction to the rocket's thrust in RocketController. I then applied
        said force in RocketModel and added a air/ground frictional component that would
        slowly slow the rocket down if no force was being applied.
    Stabilize The Rocket
        Stabilizing the rocket only required fixing the rotation with
        body.setFixedRotation(true) to prevent any collisions from causing rotations.
ROPES AND JOINTS
    Fix The Rope Bridge
        I created and initialized a RevoluteJointDef with each body ii and body ii+1 to
        make the rope bridge.
    Fix the Spinning Platform
        I created a private class BodyDef variable, pivotDef. The Spinner construction method
        initializes this BodyDef with a kinematic body. createJoints adds this Spinner
        pivotDef and makes a RevoluteJointDef with it.

        Barrier cannot have a  static body because static bodies do not move under simulation
        and have infinite mass. Thus, you would not be able to move the platform by shooting it
        or assign a revolute joint to it.
RAG-DOLL
    Assemble the Rag-doll
        I assembled the rag-doll by making a helper function called addJoint that took in
        offset multiplying coefficients and created revolute joints at the right locations
        on the body and bounded the maximum angle of revolution.
    Extra Credit
        I edited InputController to an explode button e. When e is pressed, the ragdoll
        will destroy all of its joints and a random force will be applied to all of its joints
        for a second. I implemented this by waiting for the e input in ragdoll controller
        applying a force for 2 seconds when I did, and then calling a helper function in
        ragdollModel that destroyed the joints.
