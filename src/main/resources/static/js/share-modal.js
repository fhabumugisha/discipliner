// Fonctions pour le modal de partage
window.openShareModal = function(button) {
    const modal = document.getElementById('shareModal');
    const form = document.getElementById('invitation-form');
    const childId = button.getAttribute('data-child-id');
    
    // Charger le formulaire via HTMX
    htmx.ajax('GET', `/children/invitations/form?childId=${childId}`, {
        target: '#invitation-form',
        swap: 'innerHTML'
    });
    
    // Afficher le modal
    modal.classList.remove('hidden');
};

window.closeShareModal = function() {
    const modal = document.getElementById('shareModal');
    modal.classList.add('hidden');
    
    // Nettoyer le formulaire et le résultat
    document.getElementById('invitation-form').innerHTML = '';
    document.getElementById('invitation-result').innerHTML = '';
}; 