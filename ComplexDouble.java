

public class ComplexDouble {
    public double real, imaginary;

    public ComplexDouble(double real, double imaginary) {
        this.real = real;
        this.imaginary = imaginary;
    }

    static ComplexDouble multiply(ComplexDouble a, ComplexDouble b) {
        return new ComplexDouble(
            a.real * b.real - a.imaginary * b.imaginary,
            a.real * b.imaginary + a.imaginary * b.real
        );
    }

    static ComplexDouble add(ComplexDouble a, ComplexDouble b) {
        return new ComplexDouble(
            a.real + b.real,
            a.imaginary + b.imaginary
        );
    }

    static ComplexDouble power(ComplexDouble base, double exponent) {
         // Convert to polar form
         double modulus = Math.sqrt(base.real * base.real + base.imaginary * base.imaginary);
         double angle = Math.atan2(base.imaginary, base.real);
 
         // Raise modulus to the power of the exponent
         double newModulus = Math.pow(modulus, exponent);
 
         // Multiply angle by the exponent
         double newAngle = angle * exponent;
 
         // Convert back to rectangular form
         double resultReal = newModulus * Math.cos(newAngle);
         double resultImaginary = newModulus * Math.sin(newAngle);
 
         return new ComplexDouble(resultReal, resultImaginary);
    }
}
