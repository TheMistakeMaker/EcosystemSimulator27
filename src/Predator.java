import java.util.LinkedHashMap;
import java.util.Map;

public class Predator extends Animal {
    private Animal lastPreyTarget;
    private int chaseTicks;
    private int successfulAttacks;
    private int failedAttacks;
    private WorldModel lastKnownWorld;

    public Predator(int row, int col, Map<String, Double> customGenes, String speciesName, int generation) {
        super(row, col, customGenes, speciesName, generation);
        this.lastPreyTarget = null;
        this.chaseTicks = 0;
        this.successfulAttacks = 0;
        this.failedAttacks = 0;
        this.lastKnownWorld = null;
    }

    @Override
    protected void decideAction(WorldModel world) {
        this.lastKnownWorld = world;
        if (AnimalGeneEffects.waterSeekingDrive(this) > 0.85) {
            if (seekWater(world)) {
                return;
            }
        }

        if (AnimalGeneEffects.healthRetreatDrive(this) > 0.95 && rememberedDanger > getGene("riskTolerance")) {
            rest();
            return;
        }

        if (AnimalGeneEffects.restDesire(this) > 0.95 && caloriesRatio() > 0.35) {
            rest();
            return;
        }

        if (isReproductivelyReady() && RANDOM.nextDouble() < getGene("reproductionDrive")) {
            Animal mate = world.findNearestMate(this, AnimalGeneEffects.detectionRange(this, "social"));

            if (mate != null) {
                if (distanceTo(mate) <= 1.5) {
                    reproduceWith(mate, world);
                } else {
                    moveTowardWithSpeed(mate.getRow(), mate.getCol(), world);
                }
                return;
            }
        }

        if (shouldScavenge(world)) {
            if (scavenge(world)) {
                return;
            }
        }

        if (AnimalGeneEffects.foodSeekingDrive(this) > 0.80 || getGene("aggression") + frustration > 0.80) {
            if (hunt(world)) {
                return;
            }
        }

        patrol(world);
    }

    @Override
    public boolean canEatAnimal(Animal other) {
        return other != null && other != this && other instanceof Herbivore;
    }

    @Override
    protected Animal createChild(int row, int col, Map<String, Double> childGenes, String childSpeciesName, int childGeneration) {
        Predator child = new Predator(row, col, childGenes, childSpeciesName, childGeneration);
        child.setVariationRecord(getGenes(), "Inherited predator recombination");
        return child;
    }

    @Override
    public String getAnimalType() {
        return "Predator";
    }

    @Override
    public Map<String, String> getInspectionData() {
        Map<String, String> data = new LinkedHashMap<>(super.getInspectionData());
        data.put("Behavior State", lastPreyTarget == null ? "Patrolling / opportunistic" : "Tracking prey");
        data.put("Last Prey Target ID", lastPreyTarget == null ? "none" : String.valueOf(lastPreyTarget.getId()));
        data.put("Chase Ticks", String.valueOf(chaseTicks));
        data.put("Successful Attacks", String.valueOf(successfulAttacks));
        data.put("Failed Attacks", String.valueOf(failedAttacks));
        data.put("Prey Size Preference", format(getGene("preySizePreference")));
        data.put("Prey Fat Preference", format(getGene("preyFatPreference")));
        data.put("Ambush Preference", format(getGene("ambushPreference")));
        data.put("Scavenging Preference", format(getGene("scavengingPreference")));
        return data;
    }

    private boolean seekWater(WorldModel world) {
        int[] water = world.findNearestWater(row, col, AnimalGeneEffects.detectionRange(this, "water"));

        if (water == null) {
            return false;
        }

        if (distanceTo(water[0], water[1]) <= 1.5) {
            drink(AnimalGeneEffects.drinkAmount(this));
            return true;
        }

        return moveTowardWithSpeed(water[0], water[1], world);
    }

    private boolean shouldScavenge(WorldModel world) {
        if (caloriesRatio() > getGene("foodSeekThreshold")) {
            return false;
        }

        return RANDOM.nextDouble() < AnimalGeneEffects.scavengingChance(this, world)
                && world.findNearestCorpse(row, col, AnimalGeneEffects.carrionRange(this)) != null;
    }


    private boolean scavenge(WorldModel world) {
        int[] corpse = world.findNearestCorpse(row, col, AnimalGeneEffects.carrionRange(this));

        if (corpse == null) {
            return false;
        }

        if (corpse[0] == row && corpse[1] == col) {
            double eaten = world.eatCorpseAt(row, col, AnimalGeneEffects.carrionBiteSize(this));
            gainCalories(eaten);
            energy = Math.min(getGene("maxEnergy"), energy + eaten * getGene("energyToCalorieEfficiency"));
            return eaten > 0;
        }

        return moveTowardWithSpeed(corpse[0], corpse[1], world);
    }

    private boolean hunt(WorldModel world) {
        Animal prey = findBestPrey(world);

        if (prey == null) {
            frustration = clamp(frustration + 0.05, 0.0, 1.0);
            patrol(world);
            return true;
        }

        lastPreyTarget = prey;
        double distance = distanceTo(prey);

        if (distance <= getGene("attackRange") + 0.25) {
            attack(prey, world);
            return true;
        }

        if (AnimalGeneEffects.shouldStalk(this, distance)) {
            rest();
            return true;
        }

        if (AnimalGeneEffects.chaseDesire(this, prey, distance, countPackAllies(world, prey)) < 0.35) {
            rest();
            return true;
        }

        chaseTicks++;
        return moveTowardWithSpeed(prey.getRow(), prey.getCol(), world);
    }

    private Animal findBestPrey(WorldModel world) {
        Animal best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        double detectionRange = AnimalGeneEffects.detectionRange(this, "prey");

        for (Animal animal : world.getAnimalsSnapshot()) {
            if (!canEatAnimal(animal) || !animal.isAlive()) {
                continue;
            }

            double distance = distanceTo(animal);

            if (distance > detectionRange) {
                continue;
            }

            double score = scorePrey(animal, distance, detectionRange);

            if (score > bestScore) {
                bestScore = score;
                best = animal;
            }
        }

        return best;
    }

    private double scorePrey(Animal prey, double distance, double detectionRange) {
        return AnimalGeneEffects.predatorPreyScore(this, prey, distance, detectionRange, countPackAllies(lastKnownWorld, prey));
    }



    private int countPackAllies(WorldModel world, Animal prey) {
        if (world == null || prey == null) {
            return 0;
        }

        int allies = 0;
        double range = Math.max(1.0, getGene("stalkingDistance") + getGene("packHuntingDrive") * 5.0);

        for (Animal animal : world.getAnimalsSnapshot()) {
            if (animal != this
                    && animal instanceof Predator
                    && animal.isAlive()
                    && animal.distanceTo(prey) <= range) {
                allies++;
            }
        }

        return allies;
    }

    private void attack(Animal prey, WorldModel world) {
        if (!payActionCost(AnimalGeneEffects.attackEnergyCost(this), AnimalGeneEffects.attackCalorieCost(this))) {
            rest();
            return;
        }

        int packAllies = countPackAllies(world, prey);
        double accuracy = AnimalGeneEffects.attackAccuracy(this, prey, distanceTo(prey), packAllies);

        if (RANDOM.nextDouble() > accuracy) {
            failedAttacks++;
            frustration = clamp(frustration + 0.12, 0.0, 1.0);
            confidence = clamp(confidence - 0.04, 0.0, 1.0);
            return;
        }

        double damage = AnimalGeneEffects.attackDamage(this, prey, packAllies);

        if (AnimalGeneEffects.criticalHit(this)) {
            damage *= AnimalGeneEffects.criticalMultiplier(this);
        }

        damage = Math.max(1.0, damage);
        prey.takeDamage(damage, world);
        successfulAttacks++;
        frustration = clamp(frustration - 0.20, 0.0, 1.0);
        confidence = clamp(confidence + 0.08, 0.0, 1.0);

        if (!prey.isAlive()) {
            double eaten = world.eatCorpseAt(prey.getRow(), prey.getCol(), AnimalGeneEffects.carrionBiteSize(this) * 1.35);
            gainCalories(eaten);
            energy = Math.min(getGene("maxEnergy"), energy + eaten * getGene("energyToCalorieEfficiency"));
        }
    }

    private void patrol(WorldModel world) {
        lastPreyTarget = null;
        chaseTicks = Math.max(0, chaseTicks - 1);

        if (RANDOM.nextDouble() < AnimalGeneEffects.explorationChance(this, world) + getGene("territoriality") * 0.20) {
            moveRandomly(world);
        } else {
            rest();
        }
    }
}
