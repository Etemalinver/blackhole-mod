public class ThrownBlackHole extends Entity {
    @Override
    public void tick() {
        super.tick();
        ...
        Vec3d velocity = this.getVelocity();
        Vec3d to = this.getPos().add(velocity);
        HitResult hit = world.raycast(new RaycastContext(from, to, COLLIDER, NONE, this));
        if (hit.getType() != HitResult.Type.MISS) { setPosition(hit.getPos()); spawnHole(); return; }
        setPosition(to);
        setVelocity(velocity.multiply(0.985).add(0, -0.03, 0));
        if (this.age > 120) spawnHole();   // 飞太久也炸
    }
}
