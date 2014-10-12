package net.shuttler.alliant.world;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class World
{
    private DynamicsWorld dynamicsWorld;

    public List loadedEntityList = new ArrayList();
    protected List unloadedEntityList = new ArrayList();

    public Random randGen = new Random();

    public boolean isRemote;

    private void setUpPhysics() {
        BroadphaseInterface broadphase = new DbvtBroadphase();
        CollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
        CollisionDispatcher collisionDispatcher = new CollisionDispatcher(collisionConfiguration);
        ConstraintSolver solver = new SequentialImpulseConstraintSolver();
        dynamicsWorld = new DiscreteDynamicsWorld(collisionDispatcher, broadphase, solver, collisionConfiguration);
    }

    public void update(float dt)
    {
        dynamicsWorld.stepSimulation(dt);
    }
}
