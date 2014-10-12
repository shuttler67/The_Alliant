package net.shuttler.alliant.entity;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;
import net.shuttler.alliant.world.World;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

public abstract class Entity
{
    public World worldObj;

    public RigidBody body;
    public CollisionShape shape;

    public Quat4f orientation;
    public Vector3f position;

    public Entity(World worldObj)
    {
        this.worldObj = worldObj;
        this.orientation = new Quat4f();
    }

    public void update()
    {
        Transform worldTransform = body.getMotionState().getWorldTransform(new Transform());
        position = worldTransform.origin;
        worldTransform.getRotation(orientation);
    }

    public void createBody(float mass, CollisionShape shape, Vector3f position, Quat4f orientation)
    {
        MotionState motionState = new DefaultMotionState(new Transform(new Matrix4f(orientation, position, 1.0f)));
        Vector3f inertia = new Vector3f();
        shape.calculateLocalInertia(mass, inertia);
        RigidBodyConstructionInfo constructionInfo = new RigidBodyConstructionInfo(mass, motionState, shape, inertia);
        this.body = new RigidBody(constructionInfo);
    }

    public void createBody(float mass, CollisionShape shape)
    {
        createBody(mass, shape, new Vector3f(0, 0, 0), new Quat4f(0, 0, 0, 1));
    }
}
