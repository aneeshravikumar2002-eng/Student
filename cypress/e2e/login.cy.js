describe('Student Dashboard Test', () => {

  it('Login page should open', () => {

    cy.visit('http://13.206.121.158:8080')

    cy.get('input[type="text"]')
      .type('aneesh')

    cy.get('input[type="password"]')
      .type('12345')

    cy.contains('Login')
      .click()

    cy.contains('Welcome')
  })

})
