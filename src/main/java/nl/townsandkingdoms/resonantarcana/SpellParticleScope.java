package nl.townsandkingdoms.resonantarcana;

/** Associates server particles with their source spell; nested casts restore their parent. */
final class SpellParticleScope implements AutoCloseable {
    private static final ThreadLocal<Spell> CURRENT = new ThreadLocal<>();
    private final Spell previous;
    private SpellParticleScope(Spell spell) { previous = CURRENT.get(); CURRENT.set(spell); }
    static SpellParticleScope enter(Spell spell) { return new SpellParticleScope(spell); }
    static int currentId() { Spell spell = CURRENT.get(); return spell == null ? -1 : spell.ordinal(); }
    @Override public void close() { if (previous == null) CURRENT.remove(); else CURRENT.set(previous); }
}
