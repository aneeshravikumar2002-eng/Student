describe('Student Dashboard Test', () => {

  it('Login page should open', () => {

    cy.visit('http://localhost:8000')

    cy.get('input[type="text"]')
      .type('aneesh')

    cy.get('input[type="password"]')
      .type('12345')

    cy.contains('Login')
      .click()

    cy.contains('Welcome')
  })

})
