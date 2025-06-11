const { onValueWritten } = require("firebase-functions/v2/database");
const admin = require("firebase-admin");

admin.initializeApp();

/**
 * Recalcula el rating promedio de un usuario cada vez que se escribe una reseña.
 */
exports.recalculateRating = onValueWritten(
  {
    ref: "/Users/{userId}/reviews/{reviewId}",
    region: "us-central1", // Puedes cambiar la región si lo necesitas
  },
  async (event) => {
    const snapshot = event.data.after;
    const reviewsRef = snapshot.ref.parent;
    const userRef = reviewsRef.parent;

    const reviewsSnapshot = await reviewsRef.get();

    if (!reviewsSnapshot.exists()) {
      console.log(`No hay reseñas para el usuario ${event.params.userId}. Reiniciando contadores.`);
      return userRef.update({ averageRating: 0, reviewCount: 0 });
    }

    let totalRating = 0;
    let reviewCount = 0;

    reviewsSnapshot.forEach((reviewSnap) => {
      const rating = reviewSnap.val().rating;
      if (typeof rating === "number") {
        totalRating += rating;
        reviewCount++;
      }
    });

    const averageRating = totalRating / reviewCount;

    console.log(`Actualizando perfil para ${event.params.userId}: Nuevo promedio ${averageRating.toFixed(1)}, Total reseñas ${reviewCount}`);

    return userRef.update({
      averageRating: parseFloat(averageRating.toFixed(1)),
      reviewCount: reviewCount,
    });
  }
);
